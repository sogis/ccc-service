package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.socket.WebSocketSession;


public interface SocketSender {

    public void sendMessageToApp(SessionId sessionId, AbstractMessage message) throws ServiceException;

    public void sendMessageToGis(SessionId sessionId, AbstractMessage message) throws ServiceException;

    public void sendMessageToWebSocket(WebSocketSession session, AbstractMessage message) throws ServiceException;

}