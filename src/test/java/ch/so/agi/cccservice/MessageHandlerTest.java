package ch.so.agi.cccservice;

import ch.so.agi.cccservice.exception.HandshakeIncompleteException;
import ch.so.agi.cccservice.exception.MessageMalformedException;
import ch.so.agi.cccservice.exception.MessageUnknownException;
import ch.so.agi.cccservice.exception.ReceivingConnectionClosedException;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageHandlerTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String APP_MESSAGE = """
        {
            "method": "changeLayerVisibility",
            "data": {
                "layer_identifier": "ch.so.afu.abbaustellen",
                "visible": false
            }
        }
        """;

    private static final String GIS_MESSAGE = """
        {
            "method": "notifyEditGeoObjectDone",
            "context": { "afu_geschaeft": 3671951 },
            "data": { "type": "Point", "coordinates": [2609190,1226652] }
        }
        """;

    @Test
    void malformedJson_sendsNotifyError() throws Exception {
        MockWebSocketSession sender = new MockWebSocketSession();

        MessageHandler.handleMessage(sender, "{not-json");

        JsonNode notifyError = MAPPER.readTree(sender.getLastSentTextMessage());
        assertEquals(MessageMalformedException.class.getName(), notifyError.get("nativeCode").asText());
    }

    @Test
    void unknownMessage_sendsNotifyError() throws Exception {
        String msg = """
            {
                "method": "fubar"
            }
            """;
        MockWebSocketSession sender = new MockWebSocketSession();

        MessageHandler.handleMessage(sender, msg);

        JsonNode notifyError = MAPPER.readTree(sender.getLastSentTextMessage());
        assertEquals(MessageUnknownException.class.getName(), notifyError.get("nativeCode").asText());
    }

    @Test
    void missingRequiredField_sendsNotifyError() throws Exception {
        String msg = """
            {
                "method": "connectApp",
                "apiVersion": "1.0"
            }
            """;
        MockWebSocketSession sender = new MockWebSocketSession();

        MessageHandler.handleMessage(sender, msg);

        String response = sender.getLastSentTextMessage();
        assertNotNull(response, "Expected notifyError to be sent to client");
        JsonNode notifyError = MAPPER.readTree(response);
        assertEquals("notifyError", notifyError.get("method").asText());
        assertEquals(MessageMalformedException.class.getName(), notifyError.get("nativeCode").asText());
        assertFalse(notifyError.get("message").asText().contains("uuid"), "Error message must not leak sensitive field values");
    }

    @Test
    void appDidNotCloseHandshake_sendsNotifyError() throws Exception {
        Session s = TestUtil.openSession(false);
        MessageHandler.handleMessage(s.getGisWebSocket(), GIS_MESSAGE);

        JsonNode notifyError = MAPPER.readTree(
                ((MockWebSocketSession)s.getGisWebSocket()).getLastSentTextMessage()
        );
        assertEquals(HandshakeIncompleteException.class.getName(), notifyError.get("nativeCode").asText());
    }

    @Test
    void gisDidNotCloseHandshake_sendsNotifyError() throws Exception {
        Session s = TestUtil.openSession(true);
        MessageHandler.handleMessage(s.getAppWebSocket(), APP_MESSAGE);

        JsonNode notifyError = MAPPER.readTree(
                ((MockWebSocketSession)s.getAppWebSocket()).getLastSentTextMessage()
        );
        assertEquals(HandshakeIncompleteException.class.getName(), notifyError.get("nativeCode").asText());
    }

    @Test
    void gisConnectionClosed_sendsNotifyError() throws Exception {
        Session s = TestUtil.initSession();
        MockWebSocketSession appSender = (MockWebSocketSession) s.getAppWebSocket();
        s.getGisWebSocket().close();
        MessageHandler.handleMessage(s.getAppWebSocket(), APP_MESSAGE);

        JsonNode notifyError = MAPPER.readTree(appSender.getLastSentTextMessage());
        assertEquals(ReceivingConnectionClosedException.class.getName(), notifyError.get("nativeCode").asText());
    }

    @Test
    void appConnectionClosed_sendsNotifyError() throws Exception {
        Session s = TestUtil.initSession();
        MockWebSocketSession gisSender = (MockWebSocketSession) s.getGisWebSocket();
        s.getAppWebSocket().close();
        MessageHandler.handleMessage(s.getGisWebSocket(), APP_MESSAGE);

        JsonNode notifyError = MAPPER.readTree(gisSender.getLastSentTextMessage());
        assertEquals(ReceivingConnectionClosedException.class.getName(), notifyError.get("nativeCode").asText());
    }
}
