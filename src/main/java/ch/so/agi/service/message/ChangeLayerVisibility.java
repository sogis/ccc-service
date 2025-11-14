package ch.so.agi.service.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeLayerVisibility extends Message {

    private String layerIdentifier;
    private boolean visible;

    public ChangeLayerVisibility() {
        // Default constructor for Jackson
    }

    // Jackson sets the "method" property
    @JsonProperty("method")
    public void setMethod(String method) {
        this.method = method;
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
    public void process() {
        System.out.printf("Layer '%s' visibility changed to %s%n",
                layerIdentifier, visible);
    }
}
