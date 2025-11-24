package ch.so.agi.cccservice.message.app;

import ch.so.agi.cccservice.message.Message;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeLayerVisibility extends Message {

    public static final String MESSAGE_TYPE = "changeLayerVisibility";

    @NotNull
    private String layerIdentifier;
    @NotNull
    private Boolean visible;

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

    public Boolean isVisible() {
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
