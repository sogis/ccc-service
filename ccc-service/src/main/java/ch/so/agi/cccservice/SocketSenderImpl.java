package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/** Sends messages via the already open websocket to the client (APP or GIS).
 */
@Component
public class SocketSenderImpl implements SocketSender {

    private SessionPool sessionPool;
    private JsonConverter jsonConverter = new JsonConverter();
    Logger logger = LoggerFactory.getLogger(SocketSenderImpl.class);
    
    /**
     * Constructor
     * @param sessionPool with all Sessions
     */
    public SocketSenderImpl(SessionPool sessionPool){
        this.sessionPool = sessionPool;
    }

    /**
     * Sends message to App
     * @param sessionId for identifying App
     * @param message to be sent
     * @throws ServiceException on Exception
     */
    @Override
    public void sendMessageToApp(SessionId sessionId, AbstractMessage message) throws ServiceException {
        WebSocketSession webSocketSession = sessionPool.getAppWebSocketSession(sessionId);
        SessionState sessionstate = sessionPool.getSession(sessionId);
        String clientName = sessionstate.getAppName();

        sendMessage(webSocketSession, message, clientName);
    }

    /**
     * Sends Message to WebSocketSession
     * @param webSocketSession to which message has to be sent
     * @param message to be sent
     * @param clientName of the App
     * @throws ServiceException on Exception
     */
    private void sendMessage(WebSocketSession webSocketSession, AbstractMessage message, String clientName) throws ServiceException {
        try {
            String messageString = jsonConverter.messageToString(message);

            TextMessage textMessage = new TextMessage(messageString);

            try {
                String clientIpAddress = webSocketSession.getRemoteAddress().getAddress().getHostAddress();
                logger.debug("send to "+clientIpAddress+":"+textMessage.getPayload());
                webSocketSession.sendMessage(textMessage);
            } catch (Exception e) {
                throw new ServiceException(503, "Could not send message : " + messageString + " to " + clientName +".");
            }

        } catch (JsonProcessingException e){
            throw new ServiceException(500, "Message could not be converted to string");
        }
    }

    /**
     * Sends message to GIS
     * @param sessionId for identifying GIS
     * @param message to be sent
     * @throws ServiceException on Exception
     */
    @Override
    public void sendMessageToGis(SessionId sessionId, AbstractMessage message) throws ServiceException {
        WebSocketSession webSocketSession = sessionPool.getGisWebSocketSession(sessionId);
        SessionState sessionstate = sessionPool.getSession(sessionId);
        String clientName = sessionstate.getGisName();

        sendMessage(webSocketSession, message, clientName);
    }
}