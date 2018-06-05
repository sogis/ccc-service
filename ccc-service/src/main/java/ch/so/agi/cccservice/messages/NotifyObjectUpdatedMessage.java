package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * Message sent from the domain-application to notify a change of a domain object.
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
        if(properties.getNodeType()!=JsonNodeType.ARRAY) {
            throw new IllegalArgumentException("unexpected properties type "+properties.getNodeType());
        }
        this.properties = properties;
    }
}
