package ch.so.agi.cccservice.message;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;

import ch.so.agi.cccservice.MessageHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.exception.DuplicateConnectMessageFromOtherConnectionException;
import ch.so.agi.cccservice.message.app.ConnectApp;
import ch.so.agi.cccservice.message.gis.ConnectGis;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;

class ConnectTest {

    private static final String MESSAGE_TEMPLATE = """
            {
                "method": "$METHOD",
                "clientName": "Axioma Mandant AfU",
                "apiVersion": "1.0",
                "session": "{$SESSION}"
            }
            """;

    @BeforeEach
    @SuppressWarnings("unused")
    void resetSessions() {
        Sessions.resetSessionCollection();
        Sessions.setMaxSessions(0);
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
                    "apiVersion": "1.2",
                    "session": "{019a835f-87b7-7969-ab37-53a4333c8558}"
                }
                """;

        ConnectGis con = (ConnectGis) Message.forJsonString(validJson);

        assertEquals("WGC", con.getClientName());
        assertEquals("1.2", con.getApiVersion());
        assertEquals(UUID.fromString("019a835f-87b7-7969-ab37-53a4333c8558"), con.getSessionUid());
    }

    @Test
    void firstConnect_doesNotSendNotifySessionReady() {
        Session s = TestUtil.openSession(true);

        String sent = ((MockWebSocketSession) s.getAppWebSocket()).getLastSentTextMessage();
        assertNull(sent);
    }

    @Test
    void secondConnect_sendsNotifySessionReadyOnBothConnections() {
        Session s = TestUtil.initSession();

        String sentApp = ((MockWebSocketSession) s.getAppWebSocket()).getLastSentTextMessage();
        assertTrue(sentApp.contains(SessionReady.METHOD_TYPE));

        String sentGis = ((MockWebSocketSession) s.getGisWebSocket()).getLastSentTextMessage();
        assertTrue(sentGis.contains(SessionReady.METHOD_TYPE));
    }

    @Test
    void duplicateAppConnect_FromSameSocket_SendsErrorResponse(){
        UUID sesUid = UUID.randomUUID();
        Session s = TestUtil.initSession(sesUid);

        String conApp = MESSAGE_TEMPLATE.replace("$METHOD", ConnectApp.MESSAGE_TYPE).replace("$SESSION", sesUid.toString());
        MessageHandler.handleMessage(s.getAppWebSocket(), conApp);

        String sentApp = ((MockWebSocketSession) s.getAppWebSocket()).getLastSentTextMessage();
        assertTrue(sentApp.contains(ErrorMessage.MESSAGE_TYPE));
    }

    @Test
    void duplicateGisConnect_FromSameSocket_SendsErrorResponse(){
        UUID sesUid = UUID.randomUUID();
        Session s = TestUtil.initSession(sesUid);

        String conGis = MESSAGE_TEMPLATE.replace("$METHOD", ConnectGis.MESSAGE_TYPE).replace("$SESSION", sesUid.toString());
        MessageHandler.handleMessage(s.getGisWebSocket(), conGis);

        String sentGis = ((MockWebSocketSession) s.getGisWebSocket()).getLastSentTextMessage();
        assertTrue(sentGis.contains(ErrorMessage.MESSAGE_TYPE));
    }

    @Test
    void duplicateAppConnect_FromOtherSocket_Throws(){
        UUID sesUid = UUID.randomUUID();
        TestUtil.initSession(sesUid);

        String conApp = MESSAGE_TEMPLATE.replace("$METHOD", ConnectApp.MESSAGE_TYPE).replace("$SESSION", sesUid.toString());
        MockWebSocketSession con = new MockWebSocketSession();

        Message msg = Message.forJsonString(conApp);

        assertThrows(DuplicateConnectMessageFromOtherConnectionException.class, () -> msg.process(con));
    }

    @Test
    void duplicateGisConnect_FromOtherSocket_Throws(){
        UUID sesUid = UUID.randomUUID();
        TestUtil.initSession(sesUid);

        String conGis = MESSAGE_TEMPLATE.replace("$METHOD", ConnectGis.MESSAGE_TYPE).replace("$SESSION", sesUid.toString());
        MockWebSocketSession con = new MockWebSocketSession();

        Message msg = Message.forJsonString(conGis);

        assertThrows(DuplicateConnectMessageFromOtherConnectionException.class, () -> msg.process(con));
    }

    // --- session capacity ---

    @Test
    void connect_atCapacity_rejectedWithServiceOverload() {
        // Fill one slot, then cap at exactly that count.
        TestUtil.initSession(UUID.randomUUID());
        Sessions.setMaxSessions(1);

        UUID newUid = UUID.randomUUID();
        String conApp = MESSAGE_TEMPLATE
                .replace("$METHOD", ConnectApp.MESSAGE_TYPE)
                .replace("$SESSION", newUid.toString());

        MockWebSocketSession rejected = new MockWebSocketSession();
        MessageHandler.handleMessage(rejected, conApp);

        assertFalse(rejected.isOpen(), "Rejected connection must be closed");
        assertNotNull(rejected.getLastCloseStatus());
        assertEquals(CloseStatus.SERVICE_OVERLOAD.getCode(), rejected.getLastCloseStatus().getCode());
        assertNull(Sessions.findBySessionUid(newUid), "No session must be created when at capacity");
    }

    @Test
    void connect_belowCapacity_succeeds() {
        Sessions.setMaxSessions(5);

        UUID uid = UUID.randomUUID();
        String conApp = MESSAGE_TEMPLATE
                .replace("$METHOD", ConnectApp.MESSAGE_TYPE)
                .replace("$SESSION", uid.toString());

        MockWebSocketSession con = new MockWebSocketSession();
        MessageHandler.handleMessage(con, conApp);

        assertTrue(con.isOpen());
        assertNotNull(Sessions.findBySessionUid(uid));
    }

    @Test
    void secondClientJoin_atCapacity_notRejected() {
        // First client creates session, cap is then set to current count.
        // The second client (gis) must still be able to join the existing session
        // even though we're "at capacity" — the cap applies only to NEW sessions.
        UUID sesUid = UUID.randomUUID();
        String conApp = MESSAGE_TEMPLATE
                .replace("$METHOD", ConnectApp.MESSAGE_TYPE)
                .replace("$SESSION", sesUid.toString());
        MockWebSocketSession appCon = new MockWebSocketSession();
        MessageHandler.handleMessage(appCon, conApp);
        Sessions.setMaxSessions(1);

        String conGis = MESSAGE_TEMPLATE
                .replace("$METHOD", ConnectGis.MESSAGE_TYPE)
                .replace("$SESSION", sesUid.toString());
        MockWebSocketSession gisCon = new MockWebSocketSession();
        MessageHandler.handleMessage(gisCon, conGis);

        assertTrue(gisCon.isOpen(), "Second client joining existing session must succeed despite cap");
        Session s = Sessions.findBySessionUid(sesUid);
        assertNotNull(s);
        assertNotNull(s.getAppConnection());
        assertNotNull(s.getGisConnection());
    }
}