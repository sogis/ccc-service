package ch.so.agi.cccservice.message;

import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.SockConnection;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionReadyTest {

    @Test
    public void v1_Message_MustOnlyContain_v1_Content(){
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);
        SessionReady.send(s.getAppWebSocket());

        String sent = ((MockWebSocketSession) s.getAppWebSocket()).getLastSentTextMessage();

        // v1 content
        assertTrue(sent.contains("notifySessionReady"));
        assertTrue(sent.contains(SockConnection.PROTOCOL_V1));

        // forbidden v2 content
        assertFalse(sent.contains("connectionKey"));
        assertFalse(sent.contains("sessionNr"));
    }

    @Test
    public void v2_Message_MustOnlyContain_v2_Content(){
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V2, SockConnection.PROTOCOL_V2);
        SessionReady.send(s.getAppWebSocket());

        String sent = ((MockWebSocketSession) s.getAppWebSocket()).getLastSentTextMessage();

        // v2 content
        assertTrue(sent.contains("notifySessionReady"));
        assertTrue(sent.contains(SockConnection.PROTOCOL_V2));
        assertTrue(sent.contains("connectionKey"));
        assertTrue(sent.contains("sessionNr"));
    }
}