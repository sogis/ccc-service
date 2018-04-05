package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class CancelEditGeoObjectMessage extends AbstractMessage {

    public static final String METHOD_NAME = "cancelEditGeoObject";
    private JsonNode context;

    public CancelEditGeoObjectMessage() {
        super(METHOD_NAME);
    }

    public JsonNode getContext() {
        return context;
    }

    public void setContext(JsonNode context) {
        this.context = context;
    }
}
