package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Message sent from the domain-application to start changing an existing object in the GIS-application.
 */
public class EditGeoObjectMessage extends AbstractMessage  {

    public static final String METHOD_NAME = "editGeoObject";
    private JsonNode context;
    private JsonNode data;

    /**
     * Constructor
     */
    public EditGeoObjectMessage() {
        super(METHOD_NAME);
    }

    /**
     * Gets affected object (Context)
     * @return context as Json
     */
    public JsonNode getContext() {
        return context;
    }

    /**
     * Sets affected object (context) in message
     * @param context as Json
     */
    public void setContext(JsonNode context) {
        this.context = context;
    }

    /**
     * gets changes of object
     * @return data as GeoJson
     */
    public JsonNode getData() {
        return data;
    }

    /**
     * sets changes on object in message
     * @param data as GeoJson
     */
    public void setData(JsonNode data) {
        this.data = data;
    }
}
