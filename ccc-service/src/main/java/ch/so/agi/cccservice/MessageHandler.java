package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;
import ch.so.agi.cccservice.messages.AppConnectMessage;

public class MessageHandler {
    private SessionPool sessionPool;
    private SocketSender socketSender;

    public MessageHandler(SessionPool sessionPool, SocketSender socketSender){
        this.sessionPool = sessionPool;
        this.socketSender = socketSender;
    }
    private Service service = new Service(sessionPool, socketSender);
    public void handleMessage(AbstractMessage message){

        /*if (message instanceof AppConnectMessage) {
            service.handleAppConnect(message);
        }*/
    }
}
