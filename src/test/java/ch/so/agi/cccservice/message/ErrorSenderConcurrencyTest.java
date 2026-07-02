package ch.so.agi.cccservice.message;

import ch.so.agi.cccservice.exception.MessageMalformedException;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards against ErrorSender.send() serializing error delivery across unrelated sessions.
 * As long as it is declared static synchronized, it holds a JVM-wide lock; combined with
 * WebSocketSession.sendMessage() being a blocking call with no configured send timeout
 * anywhere in this project, a single slow/unresponsive client can stall error delivery for
 * every OTHER, completely unrelated session while the lock is held.
 *
 * This test simulates that: one "attacker" session blocks inside sendMessage() (standing in
 * for a real socket whose peer never reads, so the TCP write blocks), while a second,
 * healthy "victim" session tries to receive an unrelated error message concurrently. The test
 * fails as long as the attacker can delay the victim.
 */
class ErrorSenderConcurrencyTest {

    private static final long SLOW_SEND_MILLIS = 1500;
    private static final long MAX_ACCEPTABLE_VICTIM_DELAY_MILLIS = 500;

    /** Wraps a MockWebSocketSession but blocks in sendMessage() to simulate an unresponsive peer. */
    private static class SlowWebSocketSession implements WebSocketSession {
        private final MockWebSocketSession delegate = MockWebSocketSession.create();
        private final CountDownLatch entered = new CountDownLatch(1);

        CountDownLatch enteredLatch() { return entered; }

        @Override
        public void sendMessage(WebSocketMessage<?> message) throws IOException {
            entered.countDown();
            try {
                // Simulates a blocking socket write to a client that never reads
                // (e.g. TCP receive window full). No send-timeout is configured
                // anywhere in this project (verified: no async-send-timeout /
                // BLOCKING_SEND_TIMEOUT_PROPERTY set), so in production this could
                // block indefinitely instead of a fixed 1.5s.
                Thread.sleep(SLOW_SEND_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            delegate.sendMessage(message);
        }

        @Override public String getId() { return delegate.getId(); }
        @Override public URI getUri() { return delegate.getUri(); }
        @Override public HttpHeaders getHandshakeHeaders() { return delegate.getHandshakeHeaders(); }
        @Override public Map<String, Object> getAttributes() { return delegate.getAttributes(); }
        @Override public Principal getPrincipal() { return delegate.getPrincipal(); }
        @Override public InetSocketAddress getLocalAddress() { return delegate.getLocalAddress(); }
        @Override public InetSocketAddress getRemoteAddress() { return delegate.getRemoteAddress(); }
        @Override public String getAcceptedProtocol() { return delegate.getAcceptedProtocol(); }
        @Override public void setTextMessageSizeLimit(int limit) { delegate.setTextMessageSizeLimit(limit); }
        @Override public int getTextMessageSizeLimit() { return delegate.getTextMessageSizeLimit(); }
        @Override public void setBinaryMessageSizeLimit(int limit) { delegate.setBinaryMessageSizeLimit(limit); }
        @Override public int getBinaryMessageSizeLimit() { return delegate.getBinaryMessageSizeLimit(); }
        @Override public List<WebSocketExtension> getExtensions() { return delegate.getExtensions(); }
        @Override public boolean isOpen() { return delegate.isOpen(); }
        @Override public void close() throws IOException { delegate.close(); }
        @Override public void close(CloseStatus status) throws IOException { delegate.close(status); }
    }

    @Test
    void slowClientMustNotBlockErrorDeliveryToUnrelatedSession() throws Exception {
        SlowWebSocketSession attackerSession = new SlowWebSocketSession();
        MockWebSocketSession victimSession = MockWebSocketSession.create();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            // "Attacker" thread: triggers a ClientException; sendMessage() blocks for
            // SLOW_SEND_MILLIS *while holding ErrorSender's static (class-wide) lock*.
            Future<?> attackerCall = pool.submit(() ->
                    ErrorSender.send(attackerSession, new MessageMalformedException("malformed"))
            );

            // Make sure the attacker call is actually inside sendMessage() (i.e. holding
            // the lock) before starting the victim call, to avoid a race in the assertion.
            assertTrue(attackerSession.enteredLatch().await(2, TimeUnit.SECONDS),
                    "Test setup failed: the attacker's ErrorSender.send() call never entered "
                            + "sendMessage() within 2s, so the lock-holding scenario this test "
                            + "relies on was never established. This points to a test/thread-pool "
                            + "problem, not the vulnerability itself.");

            // "Victim" thread: a completely unrelated, healthy session that also needs
            // an (unrelated) error delivered concurrently.
            long start = System.nanoTime();
            Future<?> victimCall = pool.submit(() ->
                    ErrorSender.send(victimSession, new MessageMalformedException("unrelated error"))
            );
            victimCall.get(5, TimeUnit.SECONDS);
            long elapsedMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();

            attackerCall.get(5, TimeUnit.SECONDS);

            // Regression assertion: the victim's error delivery must not be delayed by an
            // unrelated slow client. If ErrorSender.send() keeps its static (JVM-wide)
            // `synchronized`, the victim call queues up behind the attacker's held lock and
            // this fails with an elapsed time close to SLOW_SEND_MILLIS.
            assertTrue(elapsedMillis < MAX_ACCEPTABLE_VICTIM_DELAY_MILLIS, String.format(
                    "ErrorSender.send() blocked an unrelated victim session for %dms "
                            + "(allowed: < %dms). Cause: ErrorSender.send() is 'static "
                            + "synchronized', i.e. a single JVM-wide lock shared by all "
                            + "sessions; the attacker's call held that lock for the whole "
                            + "duration of its blocking sendMessage() (%dms). Impact: one "
                            + "slow/unresponsive client can stall error delivery for every "
                            + "other, unrelated session and, with enough such connections, "
                            + "exhaust the whole Tomcat worker pool (threads.max=50). "
                            + "Fix: drop or scope the lock per-connection (see SockConnection) "
                            + "and/or add a send timeout.",
                    elapsedMillis, MAX_ACCEPTABLE_VICTIM_DELAY_MILLIS, SLOW_SEND_MILLIS));
        } finally {
            pool.shutdownNow();
        }
    }
}
