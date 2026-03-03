package ch.so.agi.cccservice.message.gis;

import ch.so.agi.cccservice.JsonStringAssertions;
import ch.so.agi.cccservice.MessageHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.message.Message;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.SockConnection;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoObjectSelectedTest {

    private static final String MESSAGE = """
            {
                "method": "notifyGeoObjectSelected",
                "context_list": $CONTEXT_LIST
            }
            """;

    private static final String MESSAGE_WITH_API_VERSION = """
            {
                "method": "notifyGeoObjectSelected",
                "apiVersion": "1.2",
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

    @Test
    void process_apiVersionStrippedForLegacyApp() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V12);
        String m = MESSAGE_WITH_API_VERSION.replace("$CONTEXT_LIST", NULL);
        MessageHandler.handleMessage(s.getGisWebSocket(), m);

        String expected = MESSAGE.replace("$CONTEXT_LIST", NULL);
        JsonStringAssertions.sentMessageEquals(expected, s.getAppWebSocket());
    }

    @Test
    void process_apiVersionKeptForV12App() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        String m = MESSAGE_WITH_API_VERSION.replace("$CONTEXT_LIST", NULL);
        MessageHandler.handleMessage(s.getGisWebSocket(), m);

        JsonStringAssertions.sentMessageEquals(m, s.getAppWebSocket());
    }
}
