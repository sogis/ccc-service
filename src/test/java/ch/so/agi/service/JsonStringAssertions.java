package ch.so.agi.service;

import ch.so.agi.service.session.MockWebSocketSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.springframework.web.socket.WebSocketSession;

public class JsonStringAssertions {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void jsonStringEquals(String expectedJson, String actualJson) {
        try {
            JsonNode expectedTree = mapper.readTree(expectedJson);
            JsonNode actualTree = mapper.readTree(actualJson);
            Assertions.assertEquals(expectedTree, actualTree);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sentMessageEquals(String expectedJson, WebSocketSession conn){
        jsonStringEquals(
                expectedJson,
                ((MockWebSocketSession)conn).getLastSentTextMessage()
        );
    }
}

