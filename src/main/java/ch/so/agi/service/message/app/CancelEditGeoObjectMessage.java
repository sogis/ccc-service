package ch.so.agi.service.message.app;

import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.WebSocketSession;

/**
 * Message sent from the domain-application to cancel the editing in the GIS-application.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelEditGeoObjectMessage extends Message {

    public static final String MESSAGE_TYPE = "cancelEditGeoObject";

    @JsonProperty("context")
    private JsonNode context;

    public CancelEditGeoObjectMessage() {
        super(MESSAGE_TYPE);
    }

    public JsonNode getContext() {
        return context;
    }

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = Sessions.findByConnection(sourceConnection);
        s.assertConnected(this);
        s.getGisConnection().sendMessage(getRawMessage());
        log.info("Session {}: Cancel edit geo object", s.getSessionNr());
    }
}
