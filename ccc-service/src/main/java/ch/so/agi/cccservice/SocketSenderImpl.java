package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class SocketSenderImpl implements SocketSender {

    private SessionPool sessionPool;
    private JsonConverter jsonConverter = new JsonConverter();

    /**
     *
     * @param sessionPool
     */
    public SocketSenderImpl(SessionPool sessionPool){
        this.sessionPool = sessionPool;
    }

    /**
     *
     * @param sessionId
     * @param message
     * @throws ServiceException
     */
    @Override
    public void sendMessageToApp(SessionId sessionId, AbstractMessage message) throws ServiceException {
        WebSocketSession webSocketSession = sessionPool.getAppWebSocketSession(sessionId);
        SessionState sessionstate = sessionPool.getSession(sessionId);
        String clientName = sessionstate.getAppName();

        sendMessage(webSocketSession, message, clientName);
    }

    /**
     *
     * @param webSocketSession
     * @param message
     * @param clientName
     * @throws ServiceException
     */
    private void sendMessage(WebSocketSession webSocketSession, AbstractMessage message, String clientName) throws ServiceException {
        try {
            String messageString = jsonConverter.messageToString(message);

            TextMessage textMessage = new TextMessage(messageString);

            try {
                webSocketSession.sendMessage(textMessage);
            } catch (Exception e) {
                throw new ServiceException(503, "Could not send message : " + messageString + " to " + clientName +".");
            }

        } catch (JsonProcessingException e){
            throw new ServiceException(500, "Message could not be converted to string");
        }
    }

    /**
     *
     * @param sessionId
     * @param message
     * @throws ServiceException
     */
    @Override
    public void sendMessageToGis(SessionId sessionId, AbstractMessage message) throws ServiceException {
        WebSocketSession webSocketSession = sessionPool.getGisWebSocketSession(sessionId);
        SessionState sessionstate = sessionPool.getSession(sessionId);
        String clientName = sessionstate.getGisName();

        sendMessage(webSocketSession, message, clientName);
    }
}