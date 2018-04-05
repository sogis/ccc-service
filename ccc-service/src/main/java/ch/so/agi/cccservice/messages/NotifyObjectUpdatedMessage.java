package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class NotifyObjectUpdatedMessage extends AbstractMessage {
    public static final String METHOD_NAME = "notifyObjectUpdated";
    private JsonNode properties;

    public NotifyObjectUpdatedMessage() {
        super(METHOD_NAME);
    }

    public JsonNode getProperties() {
        return properties;
    }

    public void setProperties(JsonNode properties) {
        this.properties = properties;
    }
}
