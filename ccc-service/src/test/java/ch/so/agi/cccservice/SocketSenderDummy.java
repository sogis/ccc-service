package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

public class SocketSenderDummy implements SocketSender {
    private List<AbstractMessage> listAppMessages = new ArrayList<>();
    private List<AbstractMessage> listGisMessages = new ArrayList<>();
    private List<AbstractMessage> listWebSocketMessages = new ArrayList<>();

    @Override
    public void sendMessageToApp(SessionId sessionId, AbstractMessage message) throws ServiceException {
        listAppMessages.add(message);

    }

    public List<AbstractMessage> getAppMessages(){
        return listAppMessages;
    }

    @Override
    public void sendMessageToGis(SessionId sessionId, AbstractMessage message) throws ServiceException {
        listGisMessages.add(message);
    }

    public List<AbstractMessage> getGisMessages(){
        return listGisMessages;
    }

    @Override
    public void sendMessageToWebSocket(WebSocketSession session, AbstractMessage message) throws ServiceException{
        listWebSocketMessages.add(message);

    }
}
