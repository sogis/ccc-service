package ch.so.agi.cccservice.deamon;

import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KeyChangerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String KEYCHANGE_METHOD = "keyChange";

    @BeforeEach
    void resetSessions() {
        Sessions.resetSessionCollection();
    }

    @Test
    void sendKeyChangeHandlesEmptySessionCollection() {
        assertEquals(0, Sessions.allSessions().count());

        (new KeyChanger()).sendKeyChange();

        assertEquals(0, Sessions.allSessions().count());
    }

    @Test
    void sendKeyChangeSendsOnlyToV2Connections() throws Exception {
        Session v1Only = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);
        Session v2Only = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V2, SockConnection.PROTOCOL_V2);
        Session mixedAppV2 = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V2, SockConnection.PROTOCOL_V1);
        Session mixedGisV2 = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V2);

        (new KeyChanger()).sendKeyChange();

        String lastMessage = "";

        // v1 only
        lastMessage = ((MockWebSocketSession) v1Only.getAppWebSocket()).getLastSentTextMessage();
        assertFalse(lastMessage.contains(KEYCHANGE_METHOD));

        lastMessage = ((MockWebSocketSession) v1Only.getGisWebSocket()).getLastSentTextMessage();
        assertFalse(lastMessage.contains(KEYCHANGE_METHOD));

        // mixedAppV2
        lastMessage = ((MockWebSocketSession) mixedAppV2.getAppWebSocket()).getLastSentTextMessage();
        assertTrue(lastMessage.contains(KEYCHANGE_METHOD));

        lastMessage = ((MockWebSocketSession) mixedAppV2.getGisWebSocket()).getLastSentTextMessage();
        assertFalse(lastMessage.contains(KEYCHANGE_METHOD));

        // mixedGisV2
        lastMessage = ((MockWebSocketSession) mixedGisV2.getAppWebSocket()).getLastSentTextMessage();
        assertFalse(lastMessage.contains(KEYCHANGE_METHOD));

        lastMessage = ((MockWebSocketSession) mixedGisV2.getGisWebSocket()).getLastSentTextMessage();
        assertTrue(lastMessage.contains(KEYCHANGE_METHOD));

        // v2Only
        lastMessage = ((MockWebSocketSession) v2Only.getAppWebSocket()).getLastSentTextMessage();
        assertTrue(lastMessage.contains(KEYCHANGE_METHOD));

        lastMessage = ((MockWebSocketSession) v2Only.getGisWebSocket()).getLastSentTextMessage();
        assertTrue(lastMessage.contains(KEYCHANGE_METHOD));
    }
}