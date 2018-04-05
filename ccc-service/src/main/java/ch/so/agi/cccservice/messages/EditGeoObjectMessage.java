package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class EditGeoObjectMessage extends AbstractMessage  {
    public static final String METHOD_NAME = "editGeoObject";
    private JsonNode context;
    private JsonNode data;

    public EditGeoObjectMessage() {
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
