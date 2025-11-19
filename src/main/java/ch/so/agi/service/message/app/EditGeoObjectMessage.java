package ch.so.agi.service.message.app;

import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.WebSocketSession;

/**
 * Message sent from the domain-application to start changing an existing object in the GIS-application.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditGeoObjectMessage extends Message {

    public static final String MESSAGE_TYPE = "editGeoObject";

    @JsonProperty("context")
    private JsonNode context;
    @JsonProperty("data")
    private JsonNode data;

    public EditGeoObjectMessage() {
        super(MESSAGE_TYPE);
    }

    public JsonNode getContext() {
        return context;
    }

    public JsonNode getData() {
        return data;
    }

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = Sessions.findByConnection(sourceConnection);
        s.assertConnected(this);
        s.getGisConnection().sendMessage(getRawMessage());
        log.info("Session {}: Edit geo object", s.getSessionNr());
    }
}
