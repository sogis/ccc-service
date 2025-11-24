package ch.so.agi.cccservice.message.app;

import ch.so.agi.cccservice.JsonStringAssertions;
import ch.so.agi.cccservice.MessageHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.message.Message;
import ch.so.agi.cccservice.session.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChangeLayerVisibilityTest {
    private static final String MESSAGE = """
                {
                    "method": "changeLayerVisibility",
                    "data": {
                        "layer_identifier": "ch.so.afu.abbaustellen",
                        "visible": false
                    }
                }
            """;


    @Test
    void validJson_parses() {
        ChangeLayerVisibility vis = (ChangeLayerVisibility) Message.forJsonString(MESSAGE);

        assertEquals("ch.so.afu.abbaustellen", vis.getLayerIdentifier());
        assertFalse(vis.isVisible());
    }

    @Test
    void process_OK(){
        Session s = TestUtil.initSession();
        MessageHandler.handleMessage(s.getAppWebSocket(), MESSAGE);

        JsonStringAssertions.sentMessageEquals(MESSAGE, s.getGisWebSocket());
    }
}