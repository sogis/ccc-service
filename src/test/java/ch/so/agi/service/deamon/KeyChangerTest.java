package ch.so.agi.service.deamon;

import ch.so.agi.service.TestUtil;
import ch.so.agi.service.session.MockWebSocketSession;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import ch.so.agi.service.session.SockConnection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KeyChangeSenderTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    void resetSessions() {
        Sessions.removeAll();
    }

    @Test
    void sendKeyChangeHandlesEmptySessionCollection() {
        assertEquals(0, Sessions.allSessions().count());

        (new KeyChangeSender()).sendKeyChange();

        assertEquals(0, Sessions.allSessions().count());
    }

    @Test
    void sendKeyChangeSkipsSessionsWithOnlyV1Connections() {
        Session session = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);

        (new KeyChangeSender()).sendKeyChange();

        MockWebSocketSession appWebSocket = (MockWebSocketSession) session.getAppWebSocket();
        MockWebSocketSession gisWebSocket = (MockWebSocketSession) session.getGisWebSocket();

        assertNull(appWebSocket.getLastSentTextMessage());
        assertNull(gisWebSocket.getLastSentTextMessage());
    }

    @Test
    void sendKeyChangeSendsValidJsonToEveryV2Connection() throws Exception {
        Session v1Only = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);
        Session v2Only = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V2, SockConnection.PROTOCOL_V2);
        Session mixedAppV2 = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V2, SockConnection.PROTOCOL_V1);
        Session mixedGisV2 = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V2);

        (new KeyChangeSender()).sendKeyChange();

        assertNull(((MockWebSocketSession) v1Only.getAppWebSocket()).getLastSentTextMessage());
        assertNull(((MockWebSocketSession) v1Only.getGisWebSocket()).getLastSentTextMessage());
        assertNull(((MockWebSocketSession) mixedAppV2.getGisWebSocket()).getLastSentTextMessage());
        assertNull(((MockWebSocketSession) mixedGisV2.getAppWebSocket()).getLastSentTextMessage());

        assertNotNull(((MockWebSocketSession) mixedAppV2.getAppWebSocket()).getLastSentTextMessage());
        assertNotNull(((MockWebSocketSession) mixedGisV2.getGisWebSocket()).getLastSentTextMessage());
        assertNotNull(((MockWebSocketSession) v2Only.getAppWebSocket()).getLastSentTextMessage());
        assertNotNull(((MockWebSocketSession) v2Only.getGisWebSocket()).getLastSentTextMessage());
    }
}