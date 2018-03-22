package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class DataWrittenMessage extends AbstractMessage {
    private JsonNode properties;

    public DataWrittenMessage() {
        super("dataWritten");
    }

    public JsonNode getProperties() {
        return properties;
    }

    public void setProperties(JsonNode properties) {
        this.properties = properties;
    }
}
