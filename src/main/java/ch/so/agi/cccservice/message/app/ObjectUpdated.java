package ch.so.agi.cccservice.message.app;

import ch.so.agi.cccservice.message.Message;
import ch.so.agi.cccservice.message.validation.NotTheNullNode;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.WebSocketSession;

/**
 * Message sent from the domain-application to notify a change of a domain object.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectUpdated extends Message {

    public static final String MESSAGE_TYPE = "notifyObjectUpdated";

    @JsonProperty("properties")
    @NotTheNullNode
    private JsonNode properties;

    public ObjectUpdated() {
        super(MESSAGE_TYPE);
    }

    public JsonNode getProperties() {
        return properties;
    }

    @JsonProperty("properties")
    public void setProperties(JsonNode properties) {
        if (properties != null && !properties.isArray()) {
            throw new IllegalArgumentException("Properties must be an array");
        }
        this.properties = properties;
    }

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = Sessions.findByConnection(sourceConnection);
        s.assertConnected(this);
        s.getGisConnection().sendMessage(getRawMessage());
        log.info("Session {}: Notify object updated", s.getSessionNr());
    }
}
