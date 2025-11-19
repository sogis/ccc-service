package ch.so.agi.service.message.app;

import ch.so.agi.service.JsonStringAssertions;
import ch.so.agi.service.MessageHandler;
import ch.so.agi.service.TestUtil;
import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
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
