package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class NotifyGeoObjectSelectedMessage extends AbstractMessage  {
    public static final String METHOD_NAME = "notifyGeoObjectSelected";
    private JsonNode context_list;

    public NotifyGeoObjectSelectedMessage() {
        super(METHOD_NAME);
    }

    public JsonNode getContext_list() {
        return context_list;
    }

    public void setContext_list(JsonNode context_list) {
        this.context_list = context_list;
    }
}
