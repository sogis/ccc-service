package ch.so.agi.service;

import ch.so.agi.service.exception.HandshakeIncompleteException;
import ch.so.agi.service.exception.MessageMalformedException;
import ch.so.agi.service.exception.MessageUnknownException;
import ch.so.agi.service.exception.ReceivingConnectionClosedException;
import ch.so.agi.service.session.MockWebSocketSession;
import ch.so.agi.service.session.Session;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageHandlerTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String VALID_MESSAGE = """
            {
                "method": "changeLayerVisibility",
                "data": {
                    "layer_identifier": "ch.so.afu.abbaustellen",
                    "visible": false
                }
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
    void handshakeIncomplete_sendsNotifyError() throws Exception {
        MockWebSocketSession sender = new MockWebSocketSession();
        MessageHandler.handleMessage(sender, VALID_MESSAGE);

        JsonNode notifyError = MAPPER.readTree(sender.getLastSentTextMessage());
        assertEquals(HandshakeIncompleteException.class.getName(), notifyError.get("nativeCode").asText());
    }

    @Test
    void gisConnectionClosed_sendsNotifyError() throws Exception {
        Session s = TestUtil.initSession();
        MockWebSocketSession appSender = (MockWebSocketSession) s.getAppWebSocket();
        s.getGisWebSocket().close();
        MessageHandler.handleMessage(s.getAppWebSocket(), VALID_MESSAGE);

        JsonNode notifyError = MAPPER.readTree(appSender.getLastSentTextMessage());
        assertEquals(ReceivingConnectionClosedException.class.getName(), notifyError.get("nativeCode").asText());
    }
}
