package ch.so.agi.service.message.app;

import ch.so.agi.service.JsonStringAssertions;
import ch.so.agi.service.MessageHandler;
import ch.so.agi.service.TestUtil;
import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import org.junit.jupiter.api.Test;

class CancelEditGeoObjectMessageTest {

    private static final String MESSAGE = """
            {
                "method": "cancelEditGeoObject",
                "context": {"afu_geschaeft": "3671951"}
            }
            """;

    @Test
    void validJson_parses() {
        CancelEditGeoObjectMessage msg = (CancelEditGeoObjectMessage) Message.forJsonString(MESSAGE);

        JsonStringAssertions.jsonsStringEquals("""
                {"afu_geschaeft": "3671951"}
                """, msg.getContext().toString());
    }

    @Test
    void process_OK() {
        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getAppWebSocket(), MESSAGE);

        JsonStringAssertions.sentMessageEquals(MESSAGE, s.getGisWebSocket());
    }
}
