package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Message sent from GIS on object selected
 */
public class NotifyGeoObjectSelectedMessage extends AbstractMessage  {

    public static final String METHOD_NAME = "notifyGeoObjectSelected";
    private JsonNode context_list;

    /**
     * Constructor
     */
    public NotifyGeoObjectSelectedMessage() {
        super(METHOD_NAME);
    }

    /**
     * Gets selected Objects (context_list)
     * @return context_list as Json
     */
    public JsonNode getContext_list() {
        return context_list;
    }

    /**
     * Sets selected Object (context_list) in message
     * @param context_list as Json
     */
    public void setContext_list(JsonNode context_list) {
        this.context_list = context_list;
    }
}
