package ch.so.agi.cccservice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import ch.so.agi.cccservice.message.MessageAccumulator;
import ch.so.agi.cccservice.security.ConnectionLimiter;
import ch.so.agi.cccservice.security.ConnectionRateLimiter;
import ch.so.agi.cccservice.session.Sessions;

@Component
public class CCCWebSocketHandler extends TextWebSocketHandler {

    public static final int DEFAULT_CONNECT_MSG_MAX_DELAY_SECONDS = 2;

    private static final Logger log = LoggerFactory.getLogger(CCCWebSocketHandler.class);

    private final MessageAccumulator accumulator;
    private final int connectMsgMaxDelaySeconds;

    public CCCWebSocketHandler(
            MessageAccumulator accumulator,
            @Value("${ccc.websocket.connect-msg-max-delay-seconds:" + DEFAULT_CONNECT_MSG_MAX_DELAY_SECONDS + "}") int connectMsgMaxDelaySeconds,
            @Value("${ccc.security.connection-limiter.enabled:false}") boolean connectionLimiterEnabled,
            @Value("${ccc.security.rate-limiter.enabled:false}") boolean rateLimiterEnabled
    ) {
        this.accumulator = accumulator;
        this.connectMsgMaxDelaySeconds = connectMsgMaxDelaySeconds;

        ConnectionLimiter.getInstance().setEnabled(connectionLimiterEnabled);
        ConnectionRateLimiter.getConnectLimiter().setEnabled(rateLimiterEnabled);
        ConnectionRateLimiter.getReconnectLimiter().setEnabled(rateLimiterEnabled);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        accumulator.accumulate(
                session,
                message.getPayload(),
                message.isLast(),
                fullMessage -> MessageHandler.handleMessage(session, fullMessage)
        );
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        accumulator.cleanup(session);

        String clientIp = extractClientIp(session);
        ConnectionLimiter.getInstance().recordConnectionClosed(clientIp);
    }

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String clientIp = extractClientIp(session);
        ConnectionLimiter limiter = ConnectionLimiter.getInstance();

        // Check connection limits before accepting
        if (!limiter.isConnectionAllowed(clientIp)) {
            log.warn("Connection from {} rejected due to connection limit", clientIp);
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        limiter.recordConnectionOpened(clientIp);

        Executor delayedExecutor = CompletableFuture.delayedExecutor(connectMsgMaxDelaySeconds, TimeUnit.SECONDS);
        CompletableFuture.runAsync(() -> assertClientSentConnectMessage(session), delayedExecutor);
    }

    private String extractClientIp(WebSocketSession session) {
        InetSocketAddress remoteAddress = session.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }

    private void assertClientSentConnectMessage(WebSocketSession con) {
        if (Sessions.findByConnection(con) == null && con.isOpen()) {
            try {
                con.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            log.error("Client connection from {} rejected as no (re)connect message was sent within {} sec.",
                    con.getRemoteAddress(), connectMsgMaxDelaySeconds);
        }
    }
}
