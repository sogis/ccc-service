package ch.so.agi.cccservice.message.app;

import ch.so.agi.cccservice.JsonStringAssertions;
import ch.so.agi.cccservice.MessageHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.message.Message;
import ch.so.agi.cccservice.session.Session;
import org.junit.jupiter.api.Test;

class ObjectUpdatedTest {

    private static final String MESSAGE = """
            {
                "method": "notifyObjectUpdated",
                "properties": [
                    {"name": "parzelle", "value": "SO123"}
                ]
            }
            """;

    @Test
    void validJson_parses() {
        ObjectUpdated msg = (ObjectUpdated) Message.forJsonString(MESSAGE);

        String properties = """
                [{"name": "parzelle", "value": "SO123"}]
                """;

        JsonStringAssertions.jsonStringEquals(properties, msg.getProperties().toString());
    }

    @Test
    void process_OK() {
        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getAppWebSocket(), MESSAGE);

        JsonStringAssertions.sentMessageEquals(MESSAGE, s.getGisWebSocket());
    }
}
