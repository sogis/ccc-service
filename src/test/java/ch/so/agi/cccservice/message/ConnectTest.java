package ch.so.agi.cccservice.message;

import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.message.app.ConnectApp;
import ch.so.agi.cccservice.message.gis.ConnectGis;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConnectTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void resetSessions() {
        Sessions.resetSessionCollection();
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
}