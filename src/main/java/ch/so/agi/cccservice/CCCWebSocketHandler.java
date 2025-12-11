package ch.so.agi.cccservice;

import ch.so.agi.cccservice.session.Sessions;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Component
public class CCCWebSocketHandler extends TextWebSocketHandler {

    public static final int CONNECT_MSG_MAX_DELAY_SECONDS = 2;

    private static final Logger log = LoggerFactory.getLogger(CCCWebSocketHandler.class);

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        MessageHandler.handleMessage(session, message.getPayload());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Executor delayedExecutor = CompletableFuture.delayedExecutor(CONNECT_MSG_MAX_DELAY_SECONDS, TimeUnit.SECONDS);

        CompletableFuture.runAsync(() -> assertClientSentConnectMessage(session), delayedExecutor);
    }

    private static void assertClientSentConnectMessage(WebSocketSession con){
        if(Sessions.findByConnection(con) == null) { // No connect / reconnect message was sent
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
