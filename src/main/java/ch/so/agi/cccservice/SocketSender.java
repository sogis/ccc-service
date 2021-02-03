package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;

/**
 * Interface to implementations that send ccc-messages to either GIS- or domain-application.
 */
public interface SocketSender {

    public void sendMessageToApp(SessionId sessionId, AbstractMessage message) throws ServiceException;

    public void sendMessageToGis(SessionId sessionId, AbstractMessage message) throws ServiceException ;
}