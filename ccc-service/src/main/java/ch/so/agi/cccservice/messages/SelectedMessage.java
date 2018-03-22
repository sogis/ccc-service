package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class SelectedMessage extends AbstractMessage  {
    private JsonNode context_list;

    public SelectedMessage() {
        super("selected");
    }

    public JsonNode getContext_list() {
        return context_list;
    }

    public void setContext_list(JsonNode context_list) {
        this.context_list = context_list;
    }
}
