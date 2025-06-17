package ch.so.agi.cccservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import ch.so.agi.cccservice.messages.AbstractMessage;
import ch.so.agi.cccservice.messages.ConnectAppMessage;
import ch.so.agi.cccservice.messages.ConnectGisMessage;
import ch.so.agi.cccservice.messages.NotifyErrorMessage;


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

    private final Map<String, List<String>> sessionBuffers = new ConcurrentHashMap<>();

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
                    sessionBuffers.remove(socket.getId());
                    logger.debug("Cleaned up session buffer for: " + socket.getId());
                }finally {
                    MDC.remove(MDC_KEY_SESSIONID);
                    super.afterConnectionClosed(socket, status);
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
            String socketId = socket.getId();
            String completeMessage = "";

            try {

                /////////////////////////////////////////////////////////////
                // Implementierung für Partial Messages

                // Session-spezifischen Buffer holen oder erstellen
                List<String> buffer = sessionBuffers.computeIfAbsent(
                    socketId,
                    k -> Collections.synchronizedList(new ArrayList<>())
                );

                synchronized (buffer) {
                    buffer.add(textMessage.getPayload());
                    // Überprüfen Sie, ob die vollständige Nachricht empfangen wurde
                    if (isCompleteMessage(buffer)) {
                        completeMessage = assembleCompleteMessage(buffer);
                        // Verarbeiten der vollständign Nachricht
                        logger.debug("Received and assembled complete message: \n" + completeMessage);
                        // Leeren des Puffers für die nächste Nachricht
                        buffer.clear();
                    }
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

    /// Prüfen auf vollständige JSON-Nachricht
    private boolean isCompleteMessage(List<String> buffer) {
        try {
            // Zählen Sie die geschweiften Klammern
            int openBraces = 0;
            int closeBraces = 0;

            if (buffer == null || buffer.isEmpty()) {
                return false;
            }

            for (String part : buffer) {
                openBraces += countOccurrences(part, '{');
                closeBraces += countOccurrences(part, '}');
                openBraces += countOccurrences(part, '[');
                closeBraces += countOccurrences(part, ']');
            }

            // Eine vollständige JSON-Nachricht hat die gleiche Anzahl an öffnenden und schließenden Klammern
            return openBraces > 0 && openBraces == closeBraces;

        } catch (Exception e){
            logger.warn("Error checking if message is complete", e);
            return false; // Im Zweifelsfall: nicht komplett
        }
    }

    private int countOccurrences(String str, char ch) {
        if (str == null || str.isEmpty()) {
            return 0;
        }

        // Stream-basierte Optimierung (ab Java 8)
        return (int) str.chars()
                    .filter(c -> c == ch)
                    .count();
    }

    /// Zusammensetzen der Nachrichtenteile aus dem Puffer
    private String assembleCompleteMessage(List<String> buffer) {
        if (buffer == null || buffer.isEmpty()) {
            logger.warn("Attempted to assemble message from empty buffer");
            return "";
        }
        if (buffer.size() == 1) {
            return buffer.get(0); // Optimierung für einzelne Nachricht
        }

        StringBuilder sb = new StringBuilder();
        for (String part : buffer) {
            sb.append(part);
        }
        return sb.toString();
    }

}
