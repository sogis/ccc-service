package ch.so.agi.service.message.app;

import ch.so.agi.service.message.Message;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeLayerVisibility extends Message {

    public static final String MESSAGE_TYPE = "changeLayerVisibility";

    private String layerIdentifier;
    private boolean visible;

    public ChangeLayerVisibility(){ super(MESSAGE_TYPE); }

    // Jackson sets the nested "data" object
    @JsonSetter("data")
    private void unpackData(Map<String, Object> data) {
        this.layerIdentifier = (String) data.get("layer_identifier");
        this.visible = (Boolean) data.get("visible");
    }

    public String getLayerIdentifier() {
        return layerIdentifier;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = Sessions.findByConnection(sourceConnection);
        s.assertConnected(this);
        s.getGisConnection().sendMessage(getRawMessage());
        log.info("Session {}: Changed visibility of layer {} to {}", s.getSessionNr(), getLayerIdentifier(), isVisible());
    }
}
