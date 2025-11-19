package ch.so.agi.service.message.app;

import ch.so.agi.service.JsonStringAssertions;
import ch.so.agi.service.MessageHandler;
import ch.so.agi.service.TestUtil;
import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import org.junit.jupiter.api.Test;

class EditGeoObjectMessageTest {

    private static final String MESSAGE = """
            {
                "method": "editGeoObject",
                "context": {"afu_geschaeft": "3671951"},
                "data": {"type": "Point", "coordinates": [2609190,1226652]}
            }
            """;

    @Test
    void validJson_parses() {
        EditGeoObjectMessage msg = (EditGeoObjectMessage) Message.forJsonString(MESSAGE);

        JsonStringAssertions.jsonsStringEquals("""
                {"afu_geschaeft": "3671951"}
                """, msg.getContext().toString());
        JsonStringAssertions.jsonsStringEquals("""
                {"type": "Point", "coordinates": [2609190,1226652]}
                """, msg.getData().toString());
    }

    @Test
    void process_OK() {
        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getAppWebSocket(), MESSAGE);

        JsonStringAssertions.sentMessageEquals(MESSAGE, s.getGisWebSocket());
    }
}
