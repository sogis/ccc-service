package ch.so.agi.cccservice.messages;


import com.fasterxml.jackson.databind.JsonNode;

/**
 * Message sent from the domain-application to start recording a new object in the GIS-application.
 */
public class CreateGeoObjectMessage extends AbstractMessage {

    public static final String METHOD_NAME = "createGeoObject";
    private JsonNode context;
    private JsonNode zoomTo;

    /**
     * Constructor
     */
    public CreateGeoObjectMessage() {
        super(METHOD_NAME);
    }

    /**
     * gets affected Object (Context)
     * @return context as Json
     */
    public JsonNode getContext() {
        return context;
    }

    /**
     * Sets affected Object (Context)
     * @param context as Json
     */
    public void setContext(JsonNode context) {
        this.context = context;
    }

    /**
     * Gets value of ZoomTo
     * @return zoomTo as Json
     */
    public JsonNode getZoomTo() {
        return zoomTo;
    }

    /**
     * Sets value for ZoomTo
     * @param zoomTo Json
     */
    public void setZoomTo(JsonNode zoomTo) {
        this.zoomTo = zoomTo;
    }
}
