package ch.so.agi.service.message;

import ch.so.agi.service.message.exception.MessageParseException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Root of all classes that implement ccc-messages.
 */
abstract public class Message {

    @JsonProperty("method")
    protected String method;

    protected Message() {}

    /**
     * Registry of all message types by their "method" name.
     * (Can easily be extended as new message types are added.)
     */
    private static final Map<String, Class<? extends Message>> MESSAGE_TYPES = new HashMap<>();
    static {
        MESSAGE_TYPES.put("changeLayerVisibility", ChangeLayerVisibility.class);
    }

    /**
     * Returns a new Message-Instance for the given json string.
     * Throws a MessageParseException if the json string is not understood.
     */
    public static Message forJsonString(String json) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            // First parse the method field to decide which subclass to use
            JsonNode root = mapper.readTree(json);
            JsonNode methodNode = root.get("method");
            if (methodNode == null || !methodNode.isTextual()) {
                throw new MessageParseException(json);
            }

            String method = methodNode.asText();
            Class<? extends Message> targetType = MESSAGE_TYPES.get(method);

            if (targetType == null) {
                throw new MessageParseException(json);
            }

            // Deserialize into the discovered subclass
            return mapper.readValue(json, targetType);

        } catch (Exception e) {
            throw new MessageParseException(json, e);
        }
    }

    /** gets the ccc-method-name of message. */
    public String getMethod() {
        return method;
    }

    /** Processes the Message (to be implemented by subclasses). */
    public abstract void process();
}
