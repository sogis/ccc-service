package ch.so.agi.service.message;

import ch.so.agi.service.JsonStringAssertions;
import ch.so.agi.service.MessageHandler;
import ch.so.agi.service.TestUtil;
import ch.so.agi.service.session.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionReadyTest {

    private static final String MESSAGE = """
            {
                "method": "notifySessionReady",
                "apiVersion": "1.0"
            }
            """;

    @Test
    void validJson_parses() {
        SessionReady msg = (SessionReady) Message.forJsonString(MESSAGE);

        assertEquals("1.0", msg.getApiVersion());
    }

    @Test
    void process_OK() {
        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getAppWebSocket(), MESSAGE);

        JsonStringAssertions.sentMessageEquals(MESSAGE, s.getAppWebSocket());
        JsonStringAssertions.sentMessageEquals(MESSAGE, s.getGisWebSocket());
    }
}
