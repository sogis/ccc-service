package ch.so.agi.cccservice.message;

import ch.so.agi.cccservice.JsonStringAssertions;
import ch.so.agi.cccservice.MessageHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.session.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorTest {
    private static final String MESSAGE = """
            {
                "method": "notifyError",
                "code": %s,
                "message": %s,
                "userData": %s,
                "nativeCode": %s,
                "technicalDetails": %s
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
        String msg = String.format(MESSAGE, CODE, ERR_MESSAGE, NULL, NULL, NULL);
        Error err = (Error) Message.forJsonString(msg);

        assertEquals(CODE, err.getCode());
        assertTrue(ERR_MESSAGE.contains(err.getErrMessage()));
        assertTrue(err.getUserData().isNull());
        assertNull(err.getNativeCode());
        assertNull(err.getTechnicalDetails());
    }

    @Test
    void mandatoryAndOptionalFields_parses() {
        String msg = String.format(MESSAGE, CODE, ERR_MESSAGE, USER_DATA, NATIVE_CODE, TECH_DETAILS);
        Error err = (Error) Message.forJsonString(msg);

        assertEquals(CODE, err.getCode());
        assertTrue(ERR_MESSAGE.contains(err.getErrMessage()));
        JsonStringAssertions.jsonStringEquals(USER_DATA, err.getUserData().toString());
        assertTrue(NATIVE_CODE.contains(err.getNativeCode()));
        assertTrue(TECH_DETAILS.contains(err.getTechnicalDetails()));
    }

    @Test
    void process_OK(){
        String msg = String.format(MESSAGE, CODE, ERR_MESSAGE, NULL, NULL, NULL);

        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getAppWebSocket(), msg);

        JsonStringAssertions.sentMessageEquals(msg, s.getGisWebSocket());
    }
}