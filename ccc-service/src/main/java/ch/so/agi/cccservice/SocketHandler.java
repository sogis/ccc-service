package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;
import ch.so.agi.cccservice.messages.AppConnectMessage;
import ch.so.agi.cccservice.messages.GisConnectMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class SocketHandler extends TextWebSocketHandler {

    private SessionPool sessionPool;
    private Service service;

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        // The WebSocket has been closed

    }

    @Override

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        // The WebSocket has been opened
        // I might save this session object so that I can send messages to it outside of this method
        // Let's send the first message

        session.sendMessage(new TextMessage("You are now connected to the server. This is the first message."));

        sessionPool = new SessionPool();
    }

    @Override

    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {

        // A message has been received

        JsonConverter jsonConverter = new JsonConverter();

        System.out.println("Message received: " + textMessage.getPayload());

        AbstractMessage message = jsonConverter.stringToMessage(textMessage.getPayload());

        SocketSender socketSender = new SocketSenderImpl(sessionPool);

        service = new Service(sessionPool, socketSender);

        if (message instanceof AppConnectMessage || message instanceof GisConnectMessage) {

            if (sessionPool.getSessionId(session) != null) {
                throw new ServiceException(504, "Application is already connected.");
            }

            if (message instanceof AppConnectMessage){
                sessionPool.addAppWebSocketSession(((AppConnectMessage) message).getSession(), session);
            } else if (message instanceof GisConnectMessage){
                sessionPool.addGisWebSocketSession(((GisConnectMessage) message).getSession(), session);
            } else {
                throw new IllegalStateException();
            }
        }

        SessionId sessionId = sessionPool.getSessionId(session);

        service.handleMessage(sessionId, message);


    }

}
