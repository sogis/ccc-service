package ch.so.agi.cccservice.messages;


import com.fasterxml.jackson.databind.JsonNode;

public class ChangedMessage extends AbstractMessage {
    public static final String CHANGED = "changed";
    private JsonNode context;
    private JsonNode data;

    public ChangedMessage() {
        super(CHANGED);
    }

    public JsonNode getContext() {
        return context;
    }

    public void setContext(JsonNode context) {
        this.context = context;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }
}
