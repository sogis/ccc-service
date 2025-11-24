package ch.so.agi.cccservice.message.app;

import ch.so.agi.cccservice.JsonStringAssertions;
import ch.so.agi.cccservice.MessageHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.message.Message;
import ch.so.agi.cccservice.session.Session;
import org.junit.jupiter.api.Test;

class ShowGeoObjectTest {

    private static final String MESSAGE = """
            {
                "method": "showGeoObject",
                "context": {"afu_geschaeft": "3671951"},
                "data": {"type": "Point", "coordinates": [2609190,1226652]}
            }
            """;

    @Test
    void validJson_parses() {
        ShowGeoObject msg = (ShowGeoObject) Message.forJsonString(MESSAGE);

        String context = """
                {"afu_geschaeft": "3671951"}
                """;
        JsonStringAssertions.jsonStringEquals(context, msg.getContext().toString());

        String data = """
                {"type": "Point", "coordinates": [2609190,1226652]}
                """;
        JsonStringAssertions.jsonStringEquals(data, msg.getData().toString());
    }

    @Test
    void process_OK() {
        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getAppWebSocket(), MESSAGE);

        JsonStringAssertions.sentMessageEquals(MESSAGE, s.getGisWebSocket());
    }
}
