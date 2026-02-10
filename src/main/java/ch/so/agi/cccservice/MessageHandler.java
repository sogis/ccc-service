package ch.so.agi.cccservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import ch.so.agi.cccservice.exception.CccSecurityException;
import ch.so.agi.cccservice.exception.ClientException;
import ch.so.agi.cccservice.exception.DuplicateConnectMessageFromOtherConnectionException;
import ch.so.agi.cccservice.exception.MessageMalformedException;
import ch.so.agi.cccservice.message.ErrorSender;
import ch.so.agi.cccservice.message.Message;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import jakarta.validation.ConstraintViolationException;

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
        }
        catch (CccSecurityException se){
            log.error(se.getMessage());
            if(se instanceof DuplicateConnectMessageFromOtherConnectionException dup){
                log.debug(dup.getDebugMessage());
            }
        }
        catch (ClientException clientException) {
            Session s = Sessions.findByConnection(sender);
            if(s == null){
                log.warn("Could not find session for the received message - possibly as message is malformed or unknown. Message: '{}'. Exception: {}", message, clientException.toString());
            }
            else{
                if(m == null){
                    log.warn("Session {}: Could not parse the message '{}'. Exception: '{}'", s.getSessionNr(), message, clientException.toString());
                }
                else{
                    log.warn("Session {}: Could not execute the message '{}'. Exception: '{}'", s.getSessionNr(), m.getMessageType(), clientException.toString());
                }
            }

            ErrorSender.send(sender, clientException);
        }
        catch (ConstraintViolationException cve){
            String details = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("unknown");
            log.warn("Message validation failed!");
            log.debug("Message validation details: {}", details);
            ErrorSender.send(sender, new MessageMalformedException("Message validation failed. Check required fields."));
        }
    }
}