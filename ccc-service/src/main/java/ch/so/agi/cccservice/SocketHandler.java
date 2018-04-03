package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;
import ch.so.agi.cccservice.messages.AppConnectMessage;
import ch.so.agi.cccservice.messages.ErrorMessage;
import ch.so.agi.cccservice.messages.GisConnectMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class SocketHandler extends TextWebSocketHandler {

    Logger logger = LoggerFactory.getLogger(SocketHandler.class);

    @Autowired
    public SocketHandler(SessionPool sessionPool, Service service, SocketSender socketSender, JsonConverter jsonConverter) {
        this.sessionPool = sessionPool;
        this.service = service;
        this.socketSender = socketSender;
        this.jsonConverter = jsonConverter;
    }

    private SessionPool sessionPool;
    private Service service;
    private SocketSender socketSender;
    private JsonConverter jsonConverter;


    public void afterConnectionClosed(WebSocketSession socket, CloseStatus status) throws Exception {
        SessionId sessionId = sessionPool.getSessionId(socket);
        if(sessionId != null) {
            sessionPool.removeSession(sessionId);
            logger.info("Session "+sessionId.getSessionId()+" closed!");
        }

        // The WebSocket has been closed

    }

    @Override

    public void afterConnectionEstablished(WebSocketSession socket) throws Exception {

    }

    @Override

    protected void handleTextMessage(WebSocketSession socket, TextMessage textMessage) throws Exception {

        // A message has been received

        logger.debug(textMessage.getPayload());
        try {
            AbstractMessage message = jsonConverter.stringToMessage(textMessage.getPayload());

            if (message instanceof AppConnectMessage || message instanceof GisConnectMessage) {

                if (sessionPool.getSessionId(socket) != null) {
                    throw new ServiceException(504, "Application is already connected.");
                }

                if (message instanceof AppConnectMessage){
                    logger.info(((AppConnectMessage) message).getSession().getSessionId());
                    sessionPool.addAppWebSocketSession(((AppConnectMessage) message).getSession(), socket);
                } else if (message instanceof GisConnectMessage){
                    logger.info(((GisConnectMessage) message).getSession().getSessionId());
                    sessionPool.addGisWebSocketSession(((GisConnectMessage) message).getSession(), socket);
                } else {
                    throw new IllegalStateException();
                }
            }

            SessionId sessionId = sessionPool.getSessionId(socket);
            int clientType=sessionPool.getClientType(socket);
            service.handleMessage(clientType,sessionId, message);
        }catch(ServiceException ex) {
            logger.error("failed to handle request",ex);
            ErrorMessage msg=new ErrorMessage();
            msg.setCode(ex.getErrorCode());
            msg.setMessage(ex.getMessage());
            socket.sendMessage(new TextMessage(jsonConverter.messageToString(msg)));
        }catch(Exception ex) {
            logger.error("failed to handle request",ex);
            ErrorMessage msg=new ErrorMessage();
            msg.setCode(500);
            String msgTxt=ex.getMessage();
            if(msgTxt==null) {
                msgTxt=ex.getClass().getName();
            }
            msg.setMessage(msgTxt);
            socket.sendMessage(new TextMessage(jsonConverter.messageToString(msg)));
        }
    }

}
