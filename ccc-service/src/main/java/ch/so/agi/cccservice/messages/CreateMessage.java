package ch.so.agi.cccservice.messages;


import com.fasterxml.jackson.databind.JsonNode;

public class CreateMessage extends AbstractMessage {
    private JsonNode context;
    private JsonNode zoomTo;

    public CreateMessage() {
        super("create");
    }


    public JsonNode getContext() {
        return context;
    }

    public void setContext(JsonNode context) {
        this.context = context;
    }

    public JsonNode getZoomTo() {
        return zoomTo;
    }

    public void setZoomTo(JsonNode zoomTo) {
        this.zoomTo = zoomTo;
    }
}
