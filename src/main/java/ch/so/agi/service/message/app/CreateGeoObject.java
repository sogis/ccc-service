package ch.so.agi.service.message.app;

import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.WebSocketSession;

/**
 * Message sent from the domain-application to start recording a new object in the GIS-application.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateGeoObject extends Message {

    public static final String MESSAGE_TYPE = "createGeoObject";

    @JsonProperty("context")
    private JsonNode context;
    @JsonProperty("zoomTo")
    private JsonNode zoomTo;

    public CreateGeoObject() {
        super(MESSAGE_TYPE);
    }

    public JsonNode getContext() {
        return context;
    }

    public JsonNode getZoomTo() {
        return zoomTo;
    }

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = Sessions.findByConnection(sourceConnection);
        s.assertConnected(this);
        s.getGisConnection().sendMessage(getRawMessage());
        log.info("Session {}: Create geo object", s.getSessionNr());
    }
}
