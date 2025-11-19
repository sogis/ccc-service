package ch.so.agi.service.message.gis;

import ch.so.agi.service.JsonStringAssertions;
import ch.so.agi.service.MessageHandler;
import ch.so.agi.service.TestUtil;
import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import org.junit.jupiter.api.Test;

class EditGeoObjectDoneTest {

    private static final String MESSAGE = """
        {
            "method": "notifyEditGeoObjectDone",
            "context": { "afu_geschaeft": 3671951 },
            "data": { "type": "Point", "coordinates": [2609190,1226652] }
        }
        """;

    @Test
    void validJson_parses() {
        EditGeoObjectDone msg = (EditGeoObjectDone) Message.forJsonString(MESSAGE);

        String context = """
                "context": { "afu_geschaeft": 3671951 }
                """;
        TestUtil.jsonStringEquals(context, msg.getContext().toString());

        String data = """
                "data": { "type": "Point", "coordinates": [2609190,1226652] }
                """;
        TestUtil.jsonStringEquals(data, msg.getData().toString());
    }

    @Test
    void process_OK(){
        String message = """
        {
            "method": "notifyEditGeoObjectDone",
            "context": { "afu_geschaeft": 3671951 },
            "data": { "type": "Point", "coordinates": [2609190,1226652] }
        }
        """;

        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getGisWebSocket(), message);

        JsonStringAssertions.sentMessageEquals(message, s.getAppWebSocket());
    }
}