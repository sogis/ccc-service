package ch.so.agi.cccservice.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import ch.so.agi.cccservice.JsonStringAssertions;
import ch.so.agi.cccservice.MessageHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.SockConnection;

import java.util.UUID;

class ErrorTest {
    private static final String MESSAGE = """
            {
                "method": "notifyError",
                "code": $CODE,
                "message": $MESSAGE,
                "userData": $USER_DATA,
                "nativeCode": $NATIVE_CODE,
                "technicalDetails": $TECH_DETAILS
            }
            """;

    private static final String MESSAGE_WITH_API_VERSION = """
            {
                "method": "notifyError",
                "apiVersion": "1.2",
                "code": $CODE,
                "message": $MESSAGE,
                "userData": $USER_DATA,
                "nativeCode": $NATIVE_CODE,
                "technicalDetails": $TECH_DETAILS
            }
            """;

    private static final String USER_DATA = """
            {
                    "afu_geschaeft": 3671951,
                    "coordinates": [609190, 226652]
            }
            """;

    private static final int CODE = 464;

    private static final String ERR_MESSAGE = """
            "Coordinate must have seven digits before decimal point"
            """;

    private static final String NATIVE_CODE = """
            "Err-165"
            """;

    private static final String TECH_DETAILS = """
            "java.lang.IllegalArgumentException: at example.common.TestTry.execute(TestTry.java:17) at example.common.TestTry.main(TestTry.java:11)"
            """;

    private static final String NULL = "null";

    @Test
    void onlyMandatoryFields_parses() {
        String msg = MESSAGE.replace("$CODE", String.valueOf(CODE)).replace("$MESSAGE", ERR_MESSAGE).replace("$USER_DATA", NULL).replace("$NATIVE_CODE", NULL).replace("$TECH_DETAILS", NULL);
        ErrorMessage err = (ErrorMessage) Message.forJsonString(msg);

        assertEquals(CODE, err.getCode());
        assertTrue(ERR_MESSAGE.contains(err.getErrMessage()));
        assertTrue(err.getUserData().isNull());
        assertNull(err.getNativeCode());
        assertNull(err.getTechnicalDetails());
    }

    @Test
    void mandatoryAndOptionalFields_parses() {
        String msg = MESSAGE.replace("$CODE", String.valueOf(CODE)).replace("$MESSAGE", ERR_MESSAGE).replace("$USER_DATA", USER_DATA).replace("$NATIVE_CODE", NATIVE_CODE).replace("$TECH_DETAILS", TECH_DETAILS);
        ErrorMessage err = (ErrorMessage) Message.forJsonString(msg);

        assertEquals(CODE, err.getCode());
        assertTrue(ERR_MESSAGE.contains(err.getErrMessage()));
        JsonStringAssertions.jsonStringEquals(USER_DATA, err.getUserData().toString());
        assertTrue(NATIVE_CODE.contains(err.getNativeCode()));
        assertTrue(TECH_DETAILS.contains(err.getTechnicalDetails()));
    }

    private String buildMsg(String template) {
        return template
                .replace("$CODE", String.valueOf(CODE))
                .replace("$MESSAGE", ERR_MESSAGE)
                .replace("$USER_DATA", NULL)
                .replace("$NATIVE_CODE", NULL)
                .replace("$TECH_DETAILS", NULL);
    }

    @Test
    void process_OK(){
        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getAppWebSocket(), buildMsg(MESSAGE));

        JsonStringAssertions.sentMessageEquals(buildMsg(MESSAGE), s.getGisWebSocket());
    }

    @Test
    void process_gisToApp_apiVersionStrippedForLegacyApp() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V12);
        MessageHandler.handleMessage(s.getGisWebSocket(), buildMsg(MESSAGE_WITH_API_VERSION));

        JsonStringAssertions.sentMessageEquals(buildMsg(MESSAGE), s.getAppWebSocket());
    }

    @Test
    void process_gisToApp_apiVersionKeptForV12App() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        MessageHandler.handleMessage(s.getGisWebSocket(), buildMsg(MESSAGE_WITH_API_VERSION));

        JsonStringAssertions.sentMessageEquals(buildMsg(MESSAGE_WITH_API_VERSION), s.getAppWebSocket());
    }

    @Test
    void process_appToGis_apiVersionNotStripped() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V1);
        MessageHandler.handleMessage(s.getAppWebSocket(), buildMsg(MESSAGE_WITH_API_VERSION));

        JsonStringAssertions.sentMessageEquals(buildMsg(MESSAGE_WITH_API_VERSION), s.getGisWebSocket());
    }
}