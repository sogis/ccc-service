package ch.so.agi.cccservice.messages;


import com.fasterxml.jackson.databind.JsonNode;

public class NotifyEditGeoObjectDoneMessage extends AbstractMessage {
    public static final String METHOD_NAME = "notifyGeoObjectDone";
    private JsonNode context;
    private JsonNode data;

    public NotifyEditGeoObjectDoneMessage() {
        super(METHOD_NAME);
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
