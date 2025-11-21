package ch.so.agi.service.message.gis;

import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.WebSocketSession;

import jakarta.annotation.Nonnull;

/**
 * Message sent from the GIS-application to notify that the user selected an object.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoObjectSelected extends Message {

    public static final String MESSAGE_TYPE = "notifyGeoObjectSelected";

    private JsonNode contextList;

    public GeoObjectSelected() {
        super(MESSAGE_TYPE);
    }

    public JsonNode getContextList() {
        return contextList;
    }

    @JsonProperty("context_list")
    public void setContextList(JsonNode list) {
        if (list != null && !list.isArray()) {
            throw new IllegalArgumentException("context_list must be an array");
        }
        this.contextList = list;
    }

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = Sessions.findByConnection(sourceConnection);
        s.assertConnected(this);
        s.getAppConnection().sendMessage(getRawMessage());
        log.info("Session {}: Notify geo object selected", s.getSessionNr());
    }
}
