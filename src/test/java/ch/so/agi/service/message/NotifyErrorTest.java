package ch.so.agi.service.message;

import ch.so.agi.service.JsonStringAssertions;
import ch.so.agi.service.MessageHandler;
import ch.so.agi.service.TestUtil;
import ch.so.agi.service.session.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotifyErrorTest {
    private static final String MESSAGE = """
            {
                "method": "notifyError",
                "code": 464,
                "message": "Coordinate must have seven digits before decimal point",
                "userData": {
                    "afu_geschaeft": 3671951,
                    "coordinates": [609190, 226652]
                },
                "nativeCode": "Err-165",
                "technicalDetails": "java.lang.IllegalArgumentException: at example.common.TestTry.execute(TestTry.java:17) at example.common.TestTry.main(TestTry.java:11)"
            }
            """;

    @Test
    void validJson_parses() {
        NotifyError err = (NotifyError) Message.forJsonString(MESSAGE);

        assertEquals(464, err.getCode());
        assertEquals("Coordinate must have seven digits before decimal point", err.getRawMessage());
        assertTrue(err.getUserData().toString().contains("3671951"));
        assertEquals("Err-165", err.getNativeCode());
        assertTrue(err.getTechnicalDetails().contains("example.common.TestTry.main(TestTry.java:11)"));
    }

    @Test
    void process_OK(){
        /*
        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getAppWebSocket(), MESSAGE);

        JsonStringAssertions.sentMessageEquals(MESSAGE, s.getGisWebSocket());

         */
    }
}