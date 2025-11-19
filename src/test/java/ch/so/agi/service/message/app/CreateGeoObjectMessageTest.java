package ch.so.agi.service.message.app;

import ch.so.agi.service.JsonStringAssertions;
import ch.so.agi.service.MessageHandler;
import ch.so.agi.service.TestUtil;
import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import org.junit.jupiter.api.Test;

class CreateGeoObjectMessageTest {

    private static final String MESSAGE = """
            {
                "method": "createGeoObject",
                "context": {"afu_geschaeft": "3671951"},
                "zoomTo": {"gemeinde": 2542}
            }
            """;

    @Test
    void validJson_parses() {
        CreateGeoObjectMessage msg = (CreateGeoObjectMessage) Message.forJsonString(MESSAGE);

        JsonStringAssertions.jsonsStringEquals("""
                {"afu_geschaeft": "3671951"}
                """, msg.getContext().toString());
        JsonStringAssertions.jsonsStringEquals("""
                {"gemeinde": 2542}
                """, msg.getZoomTo().toString());
    }

    @Test
    void process_OK() {
        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getAppWebSocket(), MESSAGE);

        JsonStringAssertions.sentMessageEquals(MESSAGE, s.getGisWebSocket());
    }
}
