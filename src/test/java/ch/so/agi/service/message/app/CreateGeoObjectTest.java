package ch.so.agi.service.message.app;

import ch.so.agi.service.JsonStringAssertions;
import ch.so.agi.service.MessageHandler;
import ch.so.agi.service.TestUtil;
import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import org.junit.jupiter.api.Test;

class CreateGeoObjectTest {

    private static final String MESSAGE = """
            {
                "method": "createGeoObject",
                "context": {"afu_geschaeft": "3671951"},
                "zoomTo": {"gemeinde": 2542}
            }
            """;

    @Test
    void validJson_parses() {
        CreateGeoObject msg = (CreateGeoObject) Message.forJsonString(MESSAGE);

        String context = """
                {"afu_geschaeft": "3671951"}
                """;
        JsonStringAssertions.jsonStringEquals(context, msg.getContext().toString());

        String zoomTo = """
                {"gemeinde": 2542}
                """;
        JsonStringAssertions.jsonStringEquals(zoomTo, msg.getZoomTo().toString());
    }

    @Test
    void process_OK() {
        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getAppWebSocket(), MESSAGE);

        JsonStringAssertions.sentMessageEquals(MESSAGE, s.getGisWebSocket());
    }
}
