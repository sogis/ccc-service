package ch.so.agi.service.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeLayerVisibility extends Message {

    public static final String MESSAGE = "changeLayerVisibility";

    private String layerIdentifier;
    private boolean visible;

    public ChangeLayerVisibility() {
        // Default constructor for Jackson
    }

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
        System.out.printf("Layer '%s' visibility changed to %s%n",
                layerIdentifier, visible);
    }
}
