package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class CancelMessage extends AbstractMessage {

    public static final String CANCEL = "cancel";
    private JsonNode context;

    public CancelMessage() {
        super(CANCEL);
    }

    public JsonNode getContext() {
        return context;
    }

    public void setContext(JsonNode context) {
        this.context = context;
    }
}
