package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
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


    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        SessionId sessionId = sessionPool.getSessionId(session);
        if(sessionId != null) {
            sessionPool.removeSession(sessionId);
            logger.info("Session "+sessionId.getSessionId()+" closed!");
        }

        // The WebSocket has been closed

    }

    @Override

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    }

    @Override

    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {

        // A message has been received

        logger.debug(textMessage.getPayload());
        AbstractMessage message;


        try {

            message = jsonConverter.stringToMessage(textMessage.getPayload());

            if (message instanceof AppConnectMessage || message instanceof GisConnectMessage) {

                if (sessionPool.getSessionId(session) != null) {
                    throw new ServiceException(504, "Application is already connected.");
                }

                if (message instanceof AppConnectMessage) {
                    logger.info(((AppConnectMessage) message).getSession().getSessionId());
                    sessionPool.addAppWebSocketSession(((AppConnectMessage) message).getSession(), session);
                } else if (message instanceof GisConnectMessage) {
                    logger.info(((GisConnectMessage) message).getSession().getSessionId());
                    sessionPool.addGisWebSocketSession(((GisConnectMessage) message).getSession(), session);
                } else {
                    throw new IllegalStateException();
                }
            }

            SessionId sessionId = sessionPool.getSessionId(session);

            service.handleMessage(sessionId, message);

        } catch (ServiceException e){
            SocketSender socketSender = new SocketSenderImpl(sessionPool);
            System.out.println("--------" + e.getMessage() + "------------"+ e.getErrorCode());

            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setMessage(e.getMessage());
            errorMessage.setCode(e.getErrorCode());
            SessionId sessionId = sessionPool.getSessionId(session);
            if (sessionPool.getAppWebSocketSession(sessionId).equals(session)){
                service.handleMessage(sessionId, "app",  errorMessage);
            } else if (sessionPool.getGisWebSocketSession(sessionId).equals(session)){
                service.handleMessage(sessionId, "gis",  errorMessage);

            }

            //service.handleMessage(session, errorMessage);

            System.out.println("***************" +  jsonConverter.messageToString(errorMessage));


        }


    }

}
