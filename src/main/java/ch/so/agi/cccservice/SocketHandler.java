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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Maps the incoming web socket messages to ccc-server functionality.
 */
@Component
public class SocketHandler extends TextWebSocketHandler {

    private static final String MDC_KEY_SESSIONID = "ccc.sessionid";
    Logger logger = LoggerFactory.getLogger(SocketHandler.class);
    private ScheduledExecutorService executorService=Executors.newScheduledThreadPool(1);


    @Autowired
    public SocketHandler(SessionPool sessionPool, Service service, SocketSender socketSender, JsonConverter jsonConverter) {
        this.sessionPool = sessionPool;
        this.service = service;
        this.socketSender = socketSender;
        this.jsonConverter = jsonConverter;
        BackgroundService backgroundService=new BackgroundService(sessionPool,service.getMaxInactivityTime(),service.getPingIntervalTime());
        executorService.scheduleAtFixedRate(backgroundService, 0,service.getMaxPairingTime() , TimeUnit.SECONDS);
    }

    private SessionPool sessionPool;
    private Service service;
    private SocketSender socketSender;
    private JsonConverter jsonConverter;

    private final List<String> messageBuffer = new ArrayList<>();

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

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
/*
    private class PingTask implements Runnable{
        private WebSocketSession session;
        private Logger logger = Logger.getLogger(String.valueOf(PingTask.class));

        public PingTask(WebSocketSession session){
            this.session=session;
        }
        @Override
        public void run() {
            try {
                TextMessage pingMessage = createPingMessage();
                logger.info("Sending ping message...");
                session.sendMessage(pingMessage);
            }
            catch (Exception e){
                logger.info("Exception!! " +e);
            }
        }

        private TextMessage createPingMessage() {
            JSONObject pingMessage=new JSONObject();
            try {
                pingMessage.put("messageType",MessageType.PING_MESSAGE.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new TextMessage(pingMessage.toString());
        }
    }
 */
    @Override
    protected void handleTextMessage(WebSocketSession socket, TextMessage textMessage) throws Exception {
        try {
            // A message has been received

            String clientIpAddress = socket.getRemoteAddress().getAddress().getHostAddress();
            String completeMessage = "";
            try {

                /////////////////////////////////////////////////////////////
                // Implementierung für Partial Messages
                messageBuffer.add(textMessage.getPayload());
                // Überprüfen Sie, ob die vollständige Nachricht empfangen wurde
                if (isCompleteMessage()) {
                    completeMessage = assembleCompleteMessage();
                    // Verarbeiten Sie die vollständige Nachricht
                    logger.debug("Received and assembled complete message: \n" + completeMessage);
                    // Leeren Sie den Puffer für die nächste Nachricht
                    messageBuffer.clear();
                }
                /////////////////////////////////////////////////////////////

                logger.trace(clientIpAddress+":" + completeMessage);
                AbstractMessage message = jsonConverter.stringToMessage(completeMessage);

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
                }
            }catch(ServiceException ex) {
                logger.error("failed to handle request",ex);
                logger.info("request that failed: "+ clientIpAddress+":" + completeMessage);
                NotifyErrorMessage msg=new NotifyErrorMessage();
                msg.setCode(ex.getErrorCode());
                msg.setMessage(ex.getMessage());
                try {
                    socket.sendMessage(new TextMessage(jsonConverter.messageToString(msg)));
                }catch(IOException ex2) {
                    logger.error("failed to send error back to client",ex);
                }
            }catch(Exception ex) {
                logger.error("failed to handle request",ex);
                logger.info("request that failed: "+clientIpAddress+":"+completeMessage);
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

    private boolean isCompleteMessage() {
        // Zählen Sie die geschweiften Klammern
        int openBraces = 0;
        int closeBraces = 0;

        for (String part : messageBuffer) {
            openBraces += countOccurrences(part, '{');
            closeBraces += countOccurrences(part, '}');
            openBraces += countOccurrences(part, '[');
            closeBraces += countOccurrences(part, ']');
        }

        // Eine vollständige JSON-Nachricht hat die gleiche Anzahl an öffnenden und schließenden Klammern
        return openBraces == closeBraces;
    }

    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) {
                count++;
            }
        }
        return count;
    }

    private String assembleCompleteMessage() {
        // Kombinieren Sie die Teile der Nachricht aus dem Puffer
        return String.join("", messageBuffer);
    }

}
