package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Message to cancel changes in WebGIS. Will be sent from Application
 */
public class CancelEditGeoObjectMessage extends AbstractMessage {

    public static final String METHOD_NAME = "cancelEditGeoObject";
    private JsonNode context;

    /**
     * Constructor
     */
    public CancelEditGeoObjectMessage() {
        super(METHOD_NAME);
    }

    /**
     * Gets the specific object which has to be canceled
     * @return Object as Json
     */
    public JsonNode getContext() {
        return context;
    }

    /**
     * Sets the object, which has to be canceled
     * @param context Object as Json
     */
    public void setContext(JsonNode context) {
        this.context = context;
    }
}
