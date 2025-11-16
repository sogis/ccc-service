package ch.so.agi.service;

import ch.so.agi.service.exception.HandshakeIncompleteException;
import ch.so.agi.service.exception.MessageMalformedException;
import ch.so.agi.service.session.MockWebSocketSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageHandlerTest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String CHANGE_LAYER_MESSAGE = """
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

        JsonNode notifyError = mapper.readTree(sender.getLastSentTextMessage());
        assertEquals("notifyError", notifyError.get("method").asText());
        assertEquals(400, notifyError.get("code").asInt());
        assertEquals(MessageMalformedException.class.getName(), notifyError.get("nativeCode").asText());
    }

    @Test
    void handshakeIncomplete_sendsNotifyError() throws Exception {
        MockWebSocketSession sender = new MockWebSocketSession();
        MessageHandler.handleMessage(sender, CHANGE_LAYER_MESSAGE);

        JsonNode notifyError = mapper.readTree(sender.getLastSentTextMessage());
        assertEquals("notifyError", notifyError.get("method").asText());
        assertEquals(503, notifyError.get("code").asInt());
        assertEquals(HandshakeIncompleteException.class.getName(), notifyError.get("nativeCode").asText());
    }
}
