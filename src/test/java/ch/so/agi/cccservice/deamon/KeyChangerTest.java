package ch.so.agi.cccservice.deamon;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.message.KeyChange;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;

class KeyChangerTest {

    private static final String KEYCHANGE_METHOD = KeyChange.METHOD_TYPE;

    @BeforeEach
    void resetSessions() {
        Sessions.resetSessionCollection();
    }

    @Test
    void sendKeyChangeHandlesEmptySessionCollection() {
        assertEquals(0, Sessions.allSessions().count());

        new KeyChanger().sendKeyChange();

        assertEquals(0, Sessions.allSessions().count());
    }

    @Test
    void sendKeyChangeSendsOnlyToV12Connections() throws Exception {
        Session v1Only = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);
        Session v12Only = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        Session mixedAppV12 = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V1);
        Session mixedGisV12 = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V12);

        new KeyChanger().sendKeyChange();

        String lastMessage;

        // v1 only
        lastMessage = ((MockWebSocketSession) v1Only.getAppWebSocket()).getLastSentTextMessage();
        assertFalse(lastMessage.contains(KEYCHANGE_METHOD));

        lastMessage = ((MockWebSocketSession) v1Only.getGisWebSocket()).getLastSentTextMessage();
        assertFalse(lastMessage.contains(KEYCHANGE_METHOD));

        // mixedAppV1.2
        lastMessage = ((MockWebSocketSession) mixedAppV12.getAppWebSocket()).getLastSentTextMessage();
        assertTrue(lastMessage.contains(KEYCHANGE_METHOD));

        lastMessage = ((MockWebSocketSession) mixedAppV12.getGisWebSocket()).getLastSentTextMessage();
        assertFalse(lastMessage.contains(KEYCHANGE_METHOD));

        // mixedGisV1.2
        lastMessage = ((MockWebSocketSession) mixedGisV12.getAppWebSocket()).getLastSentTextMessage();
        assertFalse(lastMessage.contains(KEYCHANGE_METHOD));

        lastMessage = ((MockWebSocketSession) mixedGisV12.getGisWebSocket()).getLastSentTextMessage();
        assertTrue(lastMessage.contains(KEYCHANGE_METHOD));

        // v1.2 Only
        lastMessage = ((MockWebSocketSession) v12Only.getAppWebSocket()).getLastSentTextMessage();
        assertTrue(lastMessage.contains(KEYCHANGE_METHOD));

        lastMessage = ((MockWebSocketSession) v12Only.getGisWebSocket()).getLastSentTextMessage();
        assertTrue(lastMessage.contains(KEYCHANGE_METHOD));
    }
}