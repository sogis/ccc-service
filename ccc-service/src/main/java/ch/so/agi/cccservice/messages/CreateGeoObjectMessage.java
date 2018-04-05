package ch.so.agi.cccservice.messages;


import com.fasterxml.jackson.databind.JsonNode;

public class CreateGeoObjectMessage extends AbstractMessage {
    public static final String METHOD_NAME = "createGeoObject";
    private JsonNode context;
    private JsonNode zoomTo;

    public CreateGeoObjectMessage() {
        super(METHOD_NAME);
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
