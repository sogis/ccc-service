package ch.so.agi.cccservice.message.app;

import ch.so.agi.cccservice.JsonStringAssertions;
import ch.so.agi.cccservice.MessageHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.message.Message;
import ch.so.agi.cccservice.session.Session;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateGeoObjectTest {

    private static final String MESSAGE = """
            {
                "method": "createGeoObject",
                "context": %s,
                "zoomTo": %s
            }
            """;

    private static final String CONTEXT = """
                {"afu_geschaeft": "3671951"}
            """;

    private static final String ZOOM_TO = """
                {"gemeinde": 2542}
            """;

    private static final String NULL = """
                null
            """;

    @Test
    void missingContext_Throws(){
        String msg = String.format(MESSAGE, NULL, ZOOM_TO);

        assertThrows(ConstraintViolationException.class, () -> Message.forJsonString(msg));
    }

    @Test
    void onlyMandatoryFields_Parses(){
        String msg = String.format(MESSAGE, CONTEXT, NULL);

        CreateGeoObject create = (CreateGeoObject) Message.forJsonString(msg);

        JsonStringAssertions.jsonStringEquals(CONTEXT, create.getContext().toString());
        assertTrue(create.getZoomTo().isNull());
    }

    @Test
    void mandatoryAndOptionalFields_Parses(){
        String msg = String.format(MESSAGE, CONTEXT, ZOOM_TO);

        CreateGeoObject create = (CreateGeoObject) Message.forJsonString(msg);

        JsonStringAssertions.jsonStringEquals(CONTEXT, create.getContext().toString());
        JsonStringAssertions.jsonStringEquals(ZOOM_TO, create.getZoomTo().toString());
    }

    @Test
    void process_OK() {
        Session s = TestUtil.initSession();
        String msg = String.format(MESSAGE, CONTEXT, NULL);

        MessageHandler.handleMessage(s.getAppWebSocket(), msg);
        JsonStringAssertions.sentMessageEquals(msg, s.getGisWebSocket());
    }
}
