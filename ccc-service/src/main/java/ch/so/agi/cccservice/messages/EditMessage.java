package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class EditMessage extends AbstractMessage  {
    private JsonNode context;
    private JsonNode data;

    public EditMessage() {
        super("edit");
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
