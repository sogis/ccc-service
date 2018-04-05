package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Message sent from Application on changed Objects
 */
public class NotifyObjectUpdatedMessage extends AbstractMessage {
    public static final String METHOD_NAME = "notifyObjectUpdated";
    private JsonNode properties;

    /**
     * Constructor
     */
    public NotifyObjectUpdatedMessage() {
        super(METHOD_NAME);
    }

    /**
     * Gets changed object (properties)
     * @return properties as Json
     */
    public JsonNode getProperties() {
        return properties;
    }

    /**
     * Sets changed object in message
     * @param properties as Json
     */
    public void setProperties(JsonNode properties) {
        this.properties = properties;
    }
}
