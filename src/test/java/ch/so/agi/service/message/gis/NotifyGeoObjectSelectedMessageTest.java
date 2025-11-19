package ch.so.agi.service.message.gis;

import ch.so.agi.service.JsonStringAssertions;
import ch.so.agi.service.MessageHandler;
import ch.so.agi.service.TestUtil;
import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import org.junit.jupiter.api.Test;

class NotifyGeoObjectSelectedMessageTest {

    private static final String MESSAGE = """
            {
                "method": "notifyGeoObjectSelected",
                "context_list": [{"afu_geschaeft": "3671951"}]
            }
            """;

    @Test
    void validJson_parses() {
        NotifyGeoObjectSelectedMessage msg = (NotifyGeoObjectSelectedMessage) Message.forJsonString(MESSAGE);

        JsonStringAssertions.jsonsStringEquals("""
                [{"afu_geschaeft": "3671951"}]
                """, msg.getContextList().toString());
    }

    @Test
    void process_OK() {
        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getGisWebSocket(), MESSAGE);

        JsonStringAssertions.sentMessageEquals(MESSAGE, s.getAppWebSocket());
    }
}
