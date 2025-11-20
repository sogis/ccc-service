package ch.so.agi.service.message;

import ch.so.agi.service.message.app.ConnectApp;
import ch.so.agi.service.message.gis.ConnectGis;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ch.so.agi.service.session.MockWebSocketSession;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import ch.so.agi.service.session.SockConnection;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConnectTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void resetSessions() {
        Sessions.removeAll();
    }

    @Test
    void app_validJson_parses(){
        String validJson = """
                {
                    "method": "connectApp",
                    "clientName": "Axioma Mandant AfU",
                    "apiVersion": "1.0",
                    "session": "{019a835f-87b7-7969-ab37-53a4333c8558}"
                }
                """;

        ConnectApp con = (ConnectApp) Message.forJsonString(validJson);

        assertEquals("Axioma Mandant AfU", con.getClientName());
        assertEquals("1.0", con.getApiVersion());
        assertEquals(UUID.fromString("019a835f-87b7-7969-ab37-53a4333c8558"), con.getSessionUid());
    }

    @Test
    void gis_validJson_parses(){
        String validJson = """
                {
                    "method": "connectGis",
                    "clientName": "WGC",
                    "apiVersion": "2.0",
                    "session": "{019a835f-87b7-7969-ab37-53a4333c8558}"
                }
                """;

        ConnectGis con = (ConnectGis) Message.forJsonString(validJson);

        assertEquals("WGC", con.getClientName());
        assertEquals("2.0", con.getApiVersion());
        assertEquals(UUID.fromString("019a835f-87b7-7969-ab37-53a4333c8558"), con.getSessionUid());
    }

    /*
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
    */

    @Test
    void v2Connect_sendsV2NotifySessionReady() throws Exception {
        UUID sessionUid = UUID.randomUUID();
        MockWebSocketSession appConnection = new MockWebSocketSession();
        MockWebSocketSession gisConnection = new MockWebSocketSession();

        ConnectApp appConnect = (ConnectApp) Message.forJsonString(connectJson(ConnectApp.MESSAGE_TYPE,
                "app-client", SockConnection.PROTOCOL_V2, sessionUid));
        ConnectGis gisConnect = (ConnectGis) Message.forJsonString(connectJson(ConnectGis.MESSAGE_TYPE,
                "gis-client", SockConnection.PROTOCOL_V2, sessionUid));

        appConnect.process(appConnection);
        gisConnect.process(gisConnection);

        Session session = Sessions.findBySessionUid(sessionUid);
        String readyMessage = gisConnection.getLastSentTextMessage();

        assertNotNull(readyMessage);

        JsonNode readyNode = mapper.readTree(readyMessage);
        assertEquals(SessionReady.MESSAGE_TYPE, readyNode.get("method").asText());
        assertEquals(SockConnection.PROTOCOL_V2, readyNode.get("apiVersion").asText());
        assertEquals(session.getGisConnection().getConnectionKey(), readyNode.get("connection_key").asText());
        assertEquals(session.getSessionNr(), readyNode.get("session_nr").asInt());
    }

    @Test
    void v1Connect_sendsV1NotifySessionReady() {
        UUID sessionUid = UUID.randomUUID();
        MockWebSocketSession appConnection = new MockWebSocketSession();
        MockWebSocketSession gisConnection = new MockWebSocketSession();

        ConnectApp appConnect = (ConnectApp) Message.forJsonString(connectJson(ConnectApp.MESSAGE_TYPE,
                "app-client", SockConnection.PROTOCOL_V1, sessionUid));
        ConnectGis gisConnect = (ConnectGis) Message.forJsonString(connectJson(ConnectGis.MESSAGE_TYPE,
                "gis-client", SockConnection.PROTOCOL_V1, sessionUid));

        appConnect.process(appConnection);
        gisConnect.process(gisConnection);

        String readyMessage = appConnection.getLastSentTextMessage();
        String expectedMessage = """
                {
                    "method": "notifySessionReady",
                    "apiVersion": "%s",
                }
                """.formatted(SockConnection.PROTOCOL_V1);

        assertNotNull(readyMessage);
        assertEquals(expectedMessage.replaceAll("\\s", ""), readyMessage.replaceAll("\\s", ""));
        assertFalse(readyMessage.contains("connection_key"));
        assertFalse(readyMessage.contains("session_nr"));
    }

    @Test
    void firstConnect_doesNotSendNotifySessionReady() {
        UUID sessionUid = UUID.randomUUID();
        MockWebSocketSession appConnection = new MockWebSocketSession();

        ConnectApp appConnect = (ConnectApp) Message.forJsonString(connectJson(ConnectApp.MESSAGE_TYPE,
                "app-client", SockConnection.PROTOCOL_V1, sessionUid));

        appConnect.process(appConnection);

        assertTrue(appConnection.getSentTextMessages().isEmpty());
    }

    @Test
    void secondConnect_sendsNotifySessionReadyOnBothConnections() {
        UUID sessionUid = UUID.randomUUID();
        MockWebSocketSession appConnection = new MockWebSocketSession();
        MockWebSocketSession gisConnection = new MockWebSocketSession();

        ConnectApp appConnect = (ConnectApp) Message.forJsonString(connectJson(ConnectApp.MESSAGE_TYPE,
                "app-client", SockConnection.PROTOCOL_V1, sessionUid));
        ConnectGis gisConnect = (ConnectGis) Message.forJsonString(connectJson(ConnectGis.MESSAGE_TYPE,
                "gis-client", SockConnection.PROTOCOL_V1, sessionUid));

        appConnect.process(appConnection);
        gisConnect.process(gisConnection);

        assertEquals(1, appConnection.getSentTextMessages().size());
        assertEquals(1, gisConnection.getSentTextMessages().size());
        assertNotNull(appConnection.getLastSentTextMessage());
        assertNotNull(gisConnection.getLastSentTextMessage());
    }

    private String connectJson(String method, String clientName, String apiVersion, UUID sessionUid) {
        return """
                {
                    "method": "%s",
                    "clientName": "%s",
                    "apiVersion": "%s",
                    "session": "{%s}"
                }
                """.formatted(method, clientName, apiVersion, sessionUid);
    }
}