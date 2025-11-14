package ch.so.agi.service.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChangeLayerVisibilityTest {
    @Test
    void forJsonString_validJson_parses(){
        String validJson = """
                {
                    "method": "changeLayerVisibility",
                    "data": {
                        "layer_identifier": "ch.so.afu.abbaustellen",
                        "visible": false
                    }
                }
                """;

        ChangeLayerVisibility vis = (ChangeLayerVisibility) Message.forJsonString(validJson);

        assertEquals("ch.so.afu.abbaustellen", vis.getLayerIdentifier());
        assertFalse(vis.isVisible());
    }
}