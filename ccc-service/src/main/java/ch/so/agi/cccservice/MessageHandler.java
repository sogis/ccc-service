package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;
import ch.so.agi.cccservice.messages.AppConnectMessage;
import ch.so.agi.cccservice.messages.CancelMessage;
import ch.so.agi.cccservice.messages.GisConnectMessage;
import org.springframework.web.socket.WebSocketSession;

public class MessageHandler {
    private SessionPool sessionPool;
    private WebSocketSession webSocketSession;
    private SocketSender socketSender;
    private Service service = new Service(sessionPool, webSocketSession, socketSender);

    /**
     *
     * @param sessionPool
     * @param webSocketSession
     * @param socketSender
     */
    public MessageHandler(SessionPool sessionPool, WebSocketSession webSocketSession, SocketSender socketSender){
        this.sessionPool = sessionPool;
        this.webSocketSession = webSocketSession;
        this.socketSender = socketSender;
    }

    /**
     *
     * @param message
     * @throws Exception
     */
    public void handleMessage(AbstractMessage message) throws Exception{

        if (message instanceof AppConnectMessage) {
            AppConnectMessage appConnectMessage = (AppConnectMessage) message;
            service.handleAppConnect(appConnectMessage);
        }

        if (message instanceof GisConnectMessage) {
            GisConnectMessage gisConnectMessage = (GisConnectMessage) message;
            service.handleGisConnect(gisConnectMessage);
        }

        /* Todo: if (message instanceof CancelMessage){
            CancelMessage cancelMessage = (CancelMessage) message;
            service.cancel();
        }*/

        //ToDo: ChangedMessage
        //ToDo: CreateMessage
        //ToDo: DataWrittenMeassage
        //ToDo: EditMessage
        //ToDo: ErrorMessage
        //toDo: SelectedMessage
        //ToDo: ShowMessage
    }
}
