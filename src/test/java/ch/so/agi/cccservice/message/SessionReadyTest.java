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

        // forbidden v1.2 content
        assertFalse(sent.contains("connectionKey"));
        assertFalse(sent.contains("sessionNr"));
    }

    @Test
    public void v12_Message_MustOnlyContain_v12_Content(){
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        SessionReady.send(s.getAppWebSocket());

        String sent = ((MockWebSocketSession) s.getAppWebSocket()).getLastSentTextMessage();

        // v 1.2 content
        assertTrue(sent.contains("notifySessionReady"));
        assertTrue(sent.contains(SockConnection.PROTOCOL_V12));
        assertTrue(sent.contains("connectionKey"));
        assertTrue(sent.contains("sessionNr"));
    }

    @Test
    public void appReceivesAppKey_gisReceivesGisKey() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        String appKey = s.getAppConnection().getConnectionKey();
        String gisKey = s.getGisConnection().getConnectionKey();

        // Keys must be different
        assertNotEquals(appKey, gisKey, "App and GIS should have different connection keys");

        // Send SessionReady to both
        SessionReady.send(s.getAppWebSocket());
        SessionReady.send(s.getGisWebSocket());

        String sentToApp = ((MockWebSocketSession) s.getAppWebSocket()).getLastSentTextMessage();
        String sentToGis = ((MockWebSocketSession) s.getGisWebSocket()).getLastSentTextMessage();

        // App must receive App key, not GIS key
        assertTrue(sentToApp.contains(appKey), "App should receive its own connection key");
        assertFalse(sentToApp.contains(gisKey), "App should NOT receive GIS connection key");

        // GIS must receive GIS key, not App key
        assertTrue(sentToGis.contains(gisKey), "GIS should receive its own connection key");
        assertFalse(sentToGis.contains(appKey), "GIS should NOT receive App connection key");
    }
}