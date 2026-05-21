package ch.so.agi.cccservice;

import ch.so.agi.cccservice.message.MessageAccumulator;
import ch.so.agi.cccservice.security.ConnectionLimiter;
import ch.so.agi.cccservice.security.ConnectionRateLimiter;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class CCCWebSocketHandler extends TextWebSocketHandler {

    // 10s mirrors Tomcat's WebSocket idle-check granularity — values < 10s are
    // effectively rounded up by Tomcat's background process
    public static final int DEFAULT_CONNECT_MSG_MAX_DELAY_SECONDS = 10;

    private static final Logger log = LoggerFactory.getLogger(
        CCCWebSocketHandler.class
    );

    private final MessageAccumulator accumulator;
    private final int connectMsgMaxDelaySeconds;

    public CCCWebSocketHandler(
        MessageAccumulator accumulator,
        @Value(
            "${ccc.websocket.connect-msg-max-delay-seconds:" +
                DEFAULT_CONNECT_MSG_MAX_DELAY_SECONDS +
                "}"
        ) int connectMsgMaxDelaySeconds,
        @Value(
            "${ccc.security.connection-limiter.enabled:false}"
        ) boolean connectionLimiterEnabled,
        @Value(
            "${ccc.security.rate-limiter.enabled:false}"
        ) boolean rateLimiterEnabled,
        @Value("${ccc.security.max-sessions:0}") int maxSessions
    ) {
        this.accumulator = accumulator;
        this.connectMsgMaxDelaySeconds = connectMsgMaxDelaySeconds;

        ConnectionLimiter.getInstance().setEnabled(connectionLimiterEnabled);
        ConnectionRateLimiter.getConnectLimiter().setEnabled(
            rateLimiterEnabled
        );
        ConnectionRateLimiter.getReconnectLimiter().setEnabled(
            rateLimiterEnabled
        );
        Sessions.setMaxSessions(maxSessions);
        log.info(
            "Session cap: {}",
            maxSessions > 0 ? maxSessions : "unlimited"
        );
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    @Override
    protected void handleTextMessage(
        WebSocketSession session,
        TextMessage message
    ) {
        accumulator.accumulate(
            session,
            message.getPayload(),
            message.isLast(),
            fullMessage -> MessageHandler.handleMessage(session, fullMessage)
        );
    }

    @Override
    public void afterConnectionClosed(
        WebSocketSession session,
        CloseStatus status
    ) {
        accumulator.cleanup(session);

        Session cccSession = Sessions.findByConnection(session);
        if (cccSession != null) {
            cccSession.markConnectionClosed(session);

            log.info(
                "Session {}: {} connection closed. Status: {}",
                cccSession.getSessionNr(),
                cccSession.clientType(session),
                status
            );

            // V1.0 connections don't support reconnect - terminate session immediately.
            // tryInitiateTermination() guards against a race condition where both connections
            // close simultaneously, ensuring cleanup happens exactly once.
            if (
                cccSession.isV10Connection(session) &&
                cccSession.tryInitiateTermination()
            ) {
                log.info(
                    "Session {}: V1.0 connection closed - terminating session immediately (no reconnect support)",
                    cccSession.getSessionNr()
                );
                cccSession.closeConnections(
                    CloseStatus.NORMAL,
                    "Peer connection closed"
                );
                Sessions.removeSession(cccSession);
            }
        }

        String clientIp = extractClientIp(session);
        ConnectionLimiter.getInstance().recordConnectionClosed(clientIp);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session)
        throws Exception {
        String clientIp = extractClientIp(session);
        ConnectionLimiter limiter = ConnectionLimiter.getInstance();

        // Check connection limits before accepting
        if (!limiter.isConnectionAllowed(clientIp)) {
            log.warn(
                "Connection from {} rejected due to connection limit",
                clientIp
            );
            session.close(CloseStatus.SERVICE_OVERLOAD);
            return;
        }

        limiter.recordConnectionOpened(clientIp);

        // Bound the pre-Connect lifetime via Tomcat's native idle timeout. If no
        // Connect message arrives within connectMsgMaxDelaySeconds, Tomcat closes
        // the WebSocket itself. No JVM-wide CompletableFuture / ForkJoinPool task
        // is scheduled per incoming connection, so a connection storm cannot
        // starve commonPool or fill an unbounded DelayQueue.
        setIdleTimeout(session, connectMsgMaxDelaySeconds * 1000L);
    }

    /**
     * Sets the WebSocket idle timeout on the underlying jakarta.websocket Session.
     * Called once with the short pre-Connect window in afterConnectionEstablished
     * and again with 0 (disabled) once the client has completed the CCC handshake
     * (see Connect#process, Reconnect#process), so established sessions are not
     * killed by inactivity.
     *
     * @param session WebSocket session
     * @param millis  idle timeout in ms; 0 or less disables the timeout
     */
    public static void setIdleTimeout(WebSocketSession session, long millis) {
        if (session instanceof StandardWebSocketSession sws) {
            jakarta.websocket.Session nativeSession = sws.getNativeSession();
            if (nativeSession != null) {
                nativeSession.setMaxIdleTimeout(millis);
            }
        }
    }

    private String extractClientIp(WebSocketSession session) {
        InetSocketAddress remoteAddress = session.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }
}
