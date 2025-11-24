package ch.so.agi.service.message.gis;

import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.springframework.web.socket.WebSocketSession;

import jakarta.validation.constraints.NotNull;

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
        if(list == null){
            throw new IllegalArgumentException("JsonNode must not be java null");
        }

        if(!list.isNull()){
            if(!list.isArray())
                throw new IllegalArgumentException("context_list must either be a json array or json null");
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
