package ch.so.agi.cccservice;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import ch.so.agi.cccservice.message.MessageAccumulator;
import ch.so.agi.cccservice.session.Sessions;

@Component
public class CCCWebSocketHandler extends TextWebSocketHandler {

    public static final int CONNECT_MSG_MAX_DELAY_SECONDS = 2;

    private static final Logger log = LoggerFactory.getLogger(CCCWebSocketHandler.class);

    private final MessageAccumulator accumulator;

    public CCCWebSocketHandler(MessageAccumulator accumulator) {
        this.accumulator = accumulator;
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
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Executor delayedExecutor = CompletableFuture.delayedExecutor(CONNECT_MSG_MAX_DELAY_SECONDS, TimeUnit.SECONDS);

        CompletableFuture.runAsync(() -> assertClientSentConnectMessage(session), delayedExecutor);
    }

    private static void assertClientSentConnectMessage(WebSocketSession con) {
        if (Sessions.findByConnection(con) == null) { // No connect / reconnect message was sent
            try {
                con.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            log.error("Client connection from {} rejected as no (re)connect message was sent within {} sec.",
                    con.getRemoteAddress(), CONNECT_MSG_MAX_DELAY_SECONDS);
        }
    }
}
