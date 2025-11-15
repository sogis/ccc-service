package ch.so.agi.service;

import ch.so.agi.service.message.Message;
import org.springframework.web.socket.WebSocketSession;

/**
 * Helper class containing the ccc specific functionality to
 * route ccc messages through the server.
 */
public class MessageHandler {
    public static void handleMessage(WebSocketSession sender, String message){
        Message m = Message.forJsonString(message);
        m.process(sender);
    }
}

/*
Weiterfahren:
- Session: Test erweitern mit handshakes via connectApp und connectGis
-- Innerhalb graceperiod
-- Ausserhalb graceperiod

Innerhalb graceperiod ist gleichzeitig die vorbedingung für alle
*/
