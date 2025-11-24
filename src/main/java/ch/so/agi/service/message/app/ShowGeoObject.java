package ch.so.agi.service.message.app;

import ch.so.agi.service.message.Message;
import ch.so.agi.service.message.validation.NotTheNullNode;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.WebSocketSession;

import jakarta.validation.constraints.NotNull;

/**
 * Message sent from the domain-application to show an existing object in the GIS-application.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShowGeoObject extends Message {

    public static final String MESSAGE_TYPE = "showGeoObject";

    @JsonProperty("context")
    @NotTheNullNode
    private JsonNode context;
    @JsonProperty("data")
    @NotTheNullNode
    private JsonNode data;

    public ShowGeoObject() {
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
        log.info("Session {}: Show geo object", s.getSessionNr());
    }
}
