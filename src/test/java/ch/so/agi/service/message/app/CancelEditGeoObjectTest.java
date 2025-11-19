package ch.so.agi.service.message.app;

import ch.so.agi.service.JsonStringAssertions;
import ch.so.agi.service.MessageHandler;
import ch.so.agi.service.TestUtil;
import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import org.junit.jupiter.api.Test;

class CancelEditGeoObjectTest {

    private static final String MESSAGE = """
            {
                "method": "cancelEditGeoObject",
                "context": {"afu_geschaeft": "3671951"}
            }
            """;

    @Test
    void validJson_parses() {
        CancelEditGeoObject msg = (CancelEditGeoObject) Message.forJsonString(MESSAGE);

        String context = """
                {"afu_geschaeft": "3671951"}
                """;

        JsonStringAssertions.jsonStringEquals(context, msg.getContext().toString());
    }

    @Test
    void process_OK() {
        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getAppWebSocket(), MESSAGE);

        JsonStringAssertions.sentMessageEquals(MESSAGE, s.getGisWebSocket());
    }
}
