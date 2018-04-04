package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class DataWrittenMessage extends AbstractMessage {
    public static final String DATA_WRITTEN = "dataWritten";
    private JsonNode properties;

    public DataWrittenMessage() {
        super(DATA_WRITTEN);
    }

    public JsonNode getProperties() {
        return properties;
    }

    public void setProperties(JsonNode properties) {
        this.properties = properties;
    }
}
