package ch.so.agi.cccprobe;

import ch.so.agi.cccservice.JsonConverter;
import ch.so.agi.cccservice.ServiceException;
import ch.so.agi.cccservice.SessionId;
import ch.so.agi.cccservice.messages.AbstractMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        //System.out.println("AppMessage received: " + message.getPayload());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readTree(message.getPayload().toString());
        String method;
        try {
            method = obj.get("method").asText();
        } catch (NullPointerException e) {
            throw new ServiceException(400, "No method found in given JSON");
        }

        if (method.equals("ready")) {
            System.out.println("AppConnection ready");
            appReady = true;
        }
        else if (method.equals("error")) {
            System.out.println("Got Error: "+obj.get("message").asText());
        }
        else {
            System.out.println("Did not get correct message. Got: "+message.getPayload());
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

    public boolean isConnected(WebSocketHandler webSocketHandler) {
        try {
            webSocketSession.isOpen();
            return true;
        } catch(NullPointerException e){
            System.out.println("WebsocketSession is not open!");
            return false;
        }
    }
}
