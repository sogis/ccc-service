package ch.so.agi.cccprobe;

import ch.so.agi.cccservice.JsonConverter;
import ch.so.agi.cccservice.SessionId;
import ch.so.agi.cccservice.messages.AbstractMessage;
import ch.so.agi.cccservice.messages.AppConnectMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.socket.*;

public class AppClientHandler implements WebSocketHandler {

    Boolean appReady = null;

    private SessionId sessionId;
    private WebSocketSession webSocketSession;

    public Boolean getAppReady() {
        return appReady;
    }

    @Override
    public boolean supportsPartialMessages() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("Got a handleTransportError");
        // TODO Auto-generated method stub

    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

        System.out.println("AppMessage received: " + message.getPayload());
        if (appReady == null && message.getPayload().equals("{\"method\":\"ready\",\"apiVersion\":\"1.0\"}")) {
            System.out.println("AppConnection ready");
            appReady = true;
        }
        else {
            appReady = false;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webSocketSession = session;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {

    }

    public void sendMessage(AbstractMessage msg) throws Exception {
        JsonConverter jsonConverter = new JsonConverter();
        String resultingJson = jsonConverter.messageToString(msg);

        webSocketSession.sendMessage(new TextMessage(resultingJson));
    }
}
