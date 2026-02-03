package ch.so.agi.cccservice.message.gis;

import ch.so.agi.cccservice.JsonStringAssertions;
import ch.so.agi.cccservice.MessageHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.message.Message;
import ch.so.agi.cccservice.session.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoObjectSelectedTest {

    private static final String MESSAGE = """
            {
                "method": "notifyGeoObjectSelected",
                "context_list": $CONTEXT_LIST
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
        String m = MESSAGE.replace("$CONTEXT_LIST", NULL);
        GeoObjectSelected msg = (GeoObjectSelected) Message.forJsonString(m);

        assertTrue(msg.getContextList().isNull());
    }

    @Test
    void mandatoryAndOptionalFields_parses(){
        String m = MESSAGE.replace("$CONTEXT_LIST", CONTEXT_LIST);
        GeoObjectSelected msg = (GeoObjectSelected) Message.forJsonString(m);

        JsonStringAssertions.jsonStringEquals(CONTEXT_LIST, msg.getContextList().toString());
    }

    @Test
    void process_OK() {
        Session s = TestUtil.initSession();
        String m = MESSAGE.replace("$CONTEXT_LIST", NULL);
        MessageHandler.handleMessage(s.getGisWebSocket(), m);

        JsonStringAssertions.sentMessageEquals(m, s.getAppWebSocket());
    }
}
