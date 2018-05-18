package ch.so.agi.cccprobe;

import ch.so.agi.cccservice.JsonConverter;
import ch.so.agi.cccservice.ServiceException;
import ch.so.agi.cccservice.SessionId;
import ch.so.agi.cccservice.SessionPool;
import ch.so.agi.cccservice.SocketHandler;
import ch.so.agi.cccservice.messages.AbstractMessage;
import ch.so.agi.cccservice.messages.NotifyErrorMessage;
import ch.so.agi.cccservice.messages.NotifySessionReadyMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;

/**
 * The AppClientHandler class is a supporting-class for SimpleClient.
 */
public class AppClientHandler implements WebSocketHandler {

    Logger logger = LoggerFactory.getLogger(SocketHandler.class);

    Boolean appReady = null;
    private String clientName;
    private SessionId sessionId;
    private WebSocketSession webSocketSession;

    public AppClientHandler(String appClientName) {
        this.clientName=appClientName;
    }

    public Boolean getAppReady() {
        return appReady;
    }

    /**
     * Set supportsPartialMessages to false
     * @return false
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * Handle an error from the underlying WebSocket message transport and write it into the error-log.
     * @param session: The WebSocketSession where the error occurred
     * @param exception: The thrown Error
     * @throws Exception
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Got a handleTransportError ", exception);
    }

    /**
     * Handles an incoming JSON message.
     * @param session The WebSocketSession
     * @param message The incoming message. Allows only the method "ready". "error" and anything else set appReady to "false"
     * @throws Exception
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readTree(message.getPayload().toString());
        String method;
        try {
            method = obj.get("method").asText();
        } catch (NullPointerException e) {
            throw new ServiceException(400, "No method found in given JSON");
        }

        if (method.equals(NotifySessionReadyMessage.METHOD_NAME)) {
            logger.info(clientName+" "+method+" received");
            appReady = true;
        }
        else if (method.equals(NotifyErrorMessage.METHOD_NAME)) {
            logger.error("Got Error: "+obj.get("message").asText());
            appReady = false;
        }
        else {
            logger.error("Did not get correct message. Got: "+message.getPayload());
            appReady = false;
        }
    }

    /**
     *Invoked after WebSocket negotiation has succeeded and the WebSocket connection is opened and ready for use.
     * @param session The WebSocketSession
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webSocketSession = session;
    }

    /**
     * Invoked after the WebSocket connection has been closed by either side, or after a transport error has occurred.
     * Although the session may technically still be open, depending on the underlying implementation,
     * sending messages at this point is discouraged and most likely will not succeed.
     * @param session The WebSocketSession
     * @param closeStatus The close status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info(clientName+": connection closed!");
    }

    /**
     * Converts a message (class) to a string and send the resulting JSON to the WebSocket
     * @param msg The message (class)
     * @throws Exception
     */
    public void sendMessage(AbstractMessage msg) throws Exception {
        JsonConverter jsonConverter = new JsonConverter();
        String resultingJson = jsonConverter.messageToString(msg);
        webSocketSession.sendMessage(new TextMessage(resultingJson));
    }

    public boolean isConnected() {
        return webSocketSession!=null && SessionPool.isSocketOpen(webSocketSession);
    }

}
