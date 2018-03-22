package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class CancelMessage extends AbstractMessage {

    private JsonNode context;

    public CancelMessage() {
        super("cancel");
    }

    public JsonNode getContext() {
        return context;
    }

    public void setContext(JsonNode context) {
        this.context = context;
    }
}
