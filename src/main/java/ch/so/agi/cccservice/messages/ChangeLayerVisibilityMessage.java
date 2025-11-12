package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Message sent from the domain-application to change the visibility of a layer in the GIS-application.
 */
public class ChangeLayerVisibilityMessage extends AbstractMessage {

    public static final String METHOD_NAME = "changeLayerVisibility";
    private JsonNode data;

    /**
     * Constructor
     */
    public ChangeLayerVisibilityMessage() {
        super(METHOD_NAME);
    }

    /**
     * Gets the visibility information
     * @return data as Json
     */
    public JsonNode getData() {
        return data;
    }

    /**
     * Sets the visibility information
     * @param data visibility information as Json
     */
    public void setData(JsonNode data) {
        this.data = data;
    }
}
