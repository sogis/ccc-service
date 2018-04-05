package ch.so.agi.cccservice.messages;


import com.fasterxml.jackson.databind.JsonNode;

public class CreateMessage extends AbstractMessage {
    public static final String CREATE = "create";
    private JsonNode context;
    private JsonNode zoomTo;

    public CreateMessage() {
        super(CREATE);
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
