package ch.so.agi.service;

import ch.so.agi.service.exception.ClientException;
import ch.so.agi.service.message.Message;
import ch.so.agi.service.message.NotifyErrorSender;
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
        try {
            Message m = Message.forJsonString(message);
            m.setRawMessage(message);
            m.process(sender);
        } catch (ClientException clientException) {
            log.warn("Client error while processing message: {}", clientException.getMessage());
            NotifyErrorSender.send(sender, clientException);
        }
    }
}

/*
Weiterfahren:
- Session: Test erweitern mit handshakes via connectApp und connectGis
-- Innerhalb graceperiod
-- Ausserhalb graceperiod

Innerhalb graceperiod ist gleichzeitig die vorbedingung für alle
*/
