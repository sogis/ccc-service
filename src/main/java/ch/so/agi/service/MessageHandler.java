package ch.so.agi.service;

import ch.so.agi.service.exception.ClientException;
import ch.so.agi.service.message.Message;
import ch.so.agi.service.message.ErrorSender;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

/**
 * Helper class containing the ccc specific functionality to
 * route ccc messages through the server.
 */
public class MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

    public static void handleMessage(WebSocketSession sender, String message){
        Message m = null;
        try {
            m = Message.forJsonString(message);
            m.setRawMessage(message);
            m.process(sender);
        } catch (ClientException clientException) {
            Session s = Sessions.findByConnection(sender);
            if(s == null){
                log.error("Could not find session for the received message - possibly as message is malformed or unknown. Message: '{}'. Exception: {}", message, clientException.toString());
            }
            else{
                if(m == null){
                    log.error("Session {}: Could not parse the message '{}'. Exception: '{}'", s.getSessionNr(), message, clientException.toString());
                }
                else{
                    log.error("Session {}: Could not execute the message '{}'. Exception: '{}'", s.getSessionNr(), m.getMessageType(), clientException.toString());
                }
            }

            ErrorSender.send(sender, clientException);
        }
    }
}