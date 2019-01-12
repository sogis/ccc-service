package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;
import ch.so.agi.cccservice.messages.ConnectAppMessage;
import ch.so.agi.cccservice.messages.NotifyErrorMessage;
import ch.so.agi.cccservice.messages.ConnectGisMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Maps the incoming web socket messages to ccc-server functionality.
 */
@Component
public class SocketHandler extends TextWebSocketHandler {

    private static final String MDC_KEY_SESSIONID = "ccc.sessionid";
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


    @Override
    public void afterConnectionClosed(WebSocketSession socket, CloseStatus status) throws Exception {
        synchronized(sessionPool) {
            SessionId sessionId = sessionPool.getSessionId(socket);
            if(sessionId != null) {
                try {
                    MDC.put(MDC_KEY_SESSIONID, sessionId.getSessionId());
                    String clientName=sessionPool.getClientName(socket);
                    sessionPool.removeSession(sessionId);
                    logger.info("Session "+sessionId.getSessionId()+": socket closed by client "+clientName);
                    sessionPool.closeInactiveSessions(service.getMaxInactivityTime());
                }finally {
                    MDC.remove(MDC_KEY_SESSIONID);
                }
            }
        }

        // The WebSocket has been closed

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession socket) throws Exception {

    }

    @Override
    protected void handleTextMessage(WebSocketSession socket, TextMessage textMessage) throws Exception {
        try {
            // A message has been received

            String clientIpAddress = socket.getRemoteAddress().getAddress().getHostAddress();
            logger.trace(clientIpAddress+":"+textMessage.getPayload());
            try {
                AbstractMessage message = jsonConverter.stringToMessage(textMessage.getPayload());

                SessionId sessionId = null;
                synchronized(sessionPool) {
                    if (message instanceof ConnectAppMessage || message instanceof ConnectGisMessage) {

                        if (sessionPool.getSessionId(socket) != null) {
                            throw new ServiceException(504, "Application is already connected.");
                        }

                        if (message instanceof ConnectAppMessage){
                            ConnectAppMessage connectAppMessage = (ConnectAppMessage) message;
                            sessionId=connectAppMessage.getSession();
                            logger.info(clientIpAddress+":"+sessionId.getSessionId()+": "+connectAppMessage.getClientName()+": "+connectAppMessage.getMethod());
                            MDC.put(MDC_KEY_SESSIONID, sessionId.getSessionId());
                            sessionPool.addAppWebSocketSession(connectAppMessage.getSession(), socket);
                        } else if (message instanceof ConnectGisMessage){
                            ConnectGisMessage connectGisMessage = (ConnectGisMessage) message;
                            sessionId=connectGisMessage.getSession();
                            logger.info(clientIpAddress+":"+sessionId.getSessionId()+": "+connectGisMessage.getClientName()+": "+connectGisMessage.getMethod());
                            MDC.put(MDC_KEY_SESSIONID, sessionId.getSessionId());
                            sessionPool.addGisWebSocketSession(connectGisMessage.getSession(), socket);
                        } else {
                            throw new IllegalStateException();
                        }
                    }else {
                        sessionId = sessionPool.getSessionId(socket);
                        if(sessionId==null) {
                            throw new ServiceException(500,"unexpected method <"+message.getMethod()+">; client must first send "+ConnectAppMessage.METHOD_NAME+" or "+ConnectGisMessage.METHOD_NAME);
                        }
                        MDC.put(MDC_KEY_SESSIONID, sessionId.getSessionId());
                        logger.debug(clientIpAddress+": "+sessionPool.getClientName(socket)+": "+message.getMethod());
                    }
                    
                    int clientType=sessionPool.getClientType(socket);
                    if(clientType==Service.APP) {
                        service.handleAppMessage(sessionId, message);
                    }else {
                        service.handleGisMessage(sessionId, message);
                    }
                    sessionPool.closeInactiveSessions(service.getMaxInactivityTime());
                }
            }catch(ServiceException ex) {
                logger.error("failed to handle request",ex);
                logger.info("request that failed: "+clientIpAddress+":"+textMessage.getPayload());
                NotifyErrorMessage msg=new NotifyErrorMessage();
                msg.setCode(ex.getErrorCode());
                msg.setMessage(ex.getMessage());
                try {
                    socket.sendMessage(new TextMessage(jsonConverter.messageToString(msg)));
                }catch(IOException ex2) {
                    logger.error("failed to send error back to client",ex);
                }
                synchronized(sessionPool) {
                    sessionPool.closeInactiveSessions(service.getMaxInactivityTime());
                }
            }catch(Exception ex) {
                logger.error("failed to handle request",ex);
                logger.info("request that failed: "+clientIpAddress+":"+textMessage.getPayload());
                NotifyErrorMessage msg=new NotifyErrorMessage();
                msg.setCode(500);
                String msgTxt=ex.getMessage();
                if(msgTxt==null) {
                    msgTxt=ex.getClass().getName();
                }
                msg.setMessage(msgTxt);
                try {
                    socket.sendMessage(new TextMessage(jsonConverter.messageToString(msg)));
                }catch(IOException ex2) {
                    logger.error("failed to send error back to client",ex);
                }
            }
        }finally {
            MDC.remove(MDC_KEY_SESSIONID);
        }
    }

}
