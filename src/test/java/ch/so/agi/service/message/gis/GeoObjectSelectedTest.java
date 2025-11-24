package ch.so.agi.service.message.gis;

import ch.so.agi.service.JsonStringAssertions;
import ch.so.agi.service.MessageHandler;
import ch.so.agi.service.TestUtil;
import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import com.fasterxml.jackson.databind.node.NullNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoObjectSelectedTest {

    private static final String MESSAGE = """
            {
                "method": "notifyGeoObjectSelected",
                "context_list": %s
            }
            """;

    private static final String CONTEXT_LIST = """
            [{"afu_geschaeft": "3671951"}]
            """;

    private static final String NULL = """
            null
            """;

    @Test
    void mandatoryFieldsOnly_parses() {
        String m = String.format(MESSAGE, NULL);
        GeoObjectSelected msg = (GeoObjectSelected) Message.forJsonString(m);

        assertTrue(msg.getContextList().isNull());
    }

    @Test
    void mandatoryAndOptionalFields_parses(){
        String m = String.format(MESSAGE, CONTEXT_LIST);
        GeoObjectSelected msg = (GeoObjectSelected) Message.forJsonString(m);

        JsonStringAssertions.jsonStringEquals(CONTEXT_LIST, msg.getContextList().toString());
    }

    @Test
    void process_OK() {
        Session s = TestUtil.initSession();
        String m = String.format(MESSAGE, NULL);
        MessageHandler.handleMessage(s.getGisWebSocket(), m);

        JsonStringAssertions.sentMessageEquals(m, s.getAppWebSocket());
    }
}
