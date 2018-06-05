package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Message sent from the domain-application to cancel the editing in the GIS-application.
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
