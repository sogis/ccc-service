package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;

import java.util.ArrayList;
import java.util.List;

public class SocketSenderDummy implements SocketSender {
    private List<AbstractMessage> listAppMessages = new ArrayList<AbstractMessage>();
    private List<AbstractMessage> listGisMessages = new ArrayList<AbstractMessage>();

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
}
