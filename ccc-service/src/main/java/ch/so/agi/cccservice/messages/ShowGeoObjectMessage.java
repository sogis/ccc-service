package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Message sent from the domain-application to show an existing object in the GIS-application.
 */
public class ShowGeoObjectMessage extends AbstractMessage {

    public static final String METHOD_NAME = "showGeoObject";
    private JsonNode context;
    private JsonNode data;

    /**
     * Constructor
     */
    public ShowGeoObjectMessage() {
        super(METHOD_NAME);
    }

    /**
     * Gets affected object (context)
     * @return context as Json
     */
    public JsonNode getContext() {
        return context;
    }

    /**
     * Sets affected object in message
     * @param context as Json
     */
    public void setContext(JsonNode context) {
        this.context = context;
    }

    /**
     * Gets position (Data)
     * @return Data as Json
     */
    public JsonNode getData() {
        return data;
    }

    /**
     * Sets position (Data) in message
     * @param data as Json
     */
    public void setData(JsonNode data) {
        this.data = data;
    }
}
