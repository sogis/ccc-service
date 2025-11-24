package ch.so.agi.cccservice.message.gis;

import ch.so.agi.cccservice.message.Message;
import ch.so.agi.cccservice.message.validation.NotTheNullNode;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.WebSocketSession;

/**
 * Message sent from the GIS-application to notify the domain-application about a finished recording/editing
 * of an object in the GIS-application.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditGeoObjectDone extends Message {

    public static final String MESSAGE_TYPE = "notifyEditGeoObjectDone";

    public EditGeoObjectDone(){ super(MESSAGE_TYPE); }

    @JsonProperty("context")
    @NotTheNullNode
    private JsonNode context;
    @JsonProperty("data")
    private JsonNode data;

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
        s.getAppConnection().sendMessage(getRawMessage());
        log.info("Session {}: Edit geo object done sent", s.getSessionNr());
    }
}

