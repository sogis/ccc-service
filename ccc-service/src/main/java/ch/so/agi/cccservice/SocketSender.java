package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;


public interface SocketSender {

    public void sendMessageToApp(SessionId sessionId, AbstractMessage message) throws ServiceException;

    public void sendMessageToGis(SessionId sessionId, AbstractMessage message) throws ServiceException ;
}