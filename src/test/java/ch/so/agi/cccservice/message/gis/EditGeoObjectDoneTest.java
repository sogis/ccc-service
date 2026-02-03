package ch.so.agi.cccservice.message.gis;

import ch.so.agi.cccservice.JsonStringAssertions;
import ch.so.agi.cccservice.MessageHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.message.Message;
import ch.so.agi.cccservice.session.Session;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EditGeoObjectDoneTest {

    private static final String MESSAGE = """
        {
            "method": "notifyEditGeoObjectDone",
            "context": $CONTEXT,
            "data": $DATA
        }
        """;

    private static final String CONTEXT = """
        { "afu_geschaeft": 3671951 }
        """;

    private static final String DATA = """
        { "type": "Point", "coordinates": [2609190,1226652] }
        """;

    private static final String NULL = """
        null
        """;

    @Test
    void missingContext_throws(){
        String msg = MESSAGE.replace("$CONTEXT", NULL).replace("$DATA", DATA);

        assertThrows(ConstraintViolationException.class, () -> Message.forJsonString(msg));
    }

    @Test
    void onlyMandatoryFields_parses(){
        String msg = MESSAGE.replace("$CONTEXT", CONTEXT).replace("$DATA", NULL);

        EditGeoObjectDone done = (EditGeoObjectDone) Message.forJsonString(msg);

        JsonStringAssertions.jsonStringEquals(CONTEXT, done.getContext().toString());
        assertTrue(done.getData().isNull());
    }

    @Test
    void mandatoryAndOptionalFields_parses(){
        String msg = MESSAGE.replace("$CONTEXT", CONTEXT).replace("$DATA", DATA);

        EditGeoObjectDone done = (EditGeoObjectDone) Message.forJsonString(msg);

        JsonStringAssertions.jsonStringEquals(CONTEXT, done.getContext().toString());
        JsonStringAssertions.jsonStringEquals(DATA, done.getData().toString());
    }

    @Test
    void process_OK(){
        Session s = TestUtil.initSession();
        String msg = MESSAGE.replace("$CONTEXT", CONTEXT).replace("$DATA", NULL);

        MessageHandler.handleMessage(s.getGisWebSocket(), msg);

        JsonStringAssertions.sentMessageEquals(msg, s.getAppWebSocket());
    }
}