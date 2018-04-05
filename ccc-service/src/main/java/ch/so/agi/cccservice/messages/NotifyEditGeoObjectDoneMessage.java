package ch.so.agi.cccservice.messages;


import com.fasterxml.jackson.databind.JsonNode;

/**
 * Message sent by GIS on changed object
 */
public class NotifyEditGeoObjectDoneMessage extends AbstractMessage {

    public static final String METHOD_NAME = "notifyGeoObjectDone";
    private JsonNode context;
    private JsonNode data;

    /**
     * Constructor
     */
    public NotifyEditGeoObjectDoneMessage() {
        super(METHOD_NAME);
    }

    /**
     * Gets affected object (context) of message
     * @return context as Json
     */
    public JsonNode getContext() {
        return context;
    }

    /**
     * Sets affected object (context) in message
     * @param context as Josn
     */
    public void setContext(JsonNode context) {
        this.context = context;
    }

    /**
     * Gets changes on object
     * @return data as Json
     */
    public JsonNode getData() {
        return data;
    }

    /**
     * Sets changes on object in message
     * @param data as Json
     */
    public void setData(JsonNode data) {
        this.data = data;
    }
}
