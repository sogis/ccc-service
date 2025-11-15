package ch.so.agi.service.message;

import ch.so.agi.service.message.exception.MessageParseException;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import ch.so.agi.service.session.SockConnection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Root of all classes that implement ccc-messages.
 */
abstract public class Message {

    protected Message() {}

    /**
     * Registry of all message types by their "method" name.
     * (Can easily be extended as new message types are added.)
     */
    private static final Map<String, Class<? extends Message>> MESSAGE_TYPES = new HashMap<>();
    static {
        MESSAGE_TYPES.put(ChangeLayerVisibility.MESSAGE, ChangeLayerVisibility.class);
        MESSAGE_TYPES.put(ConnectApp.MESSAGE, ConnectApp.class);
        MESSAGE_TYPES.put(ConnectGis.MESSAGE, ConnectGis.class);
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

    /** Processes the Message (to be implemented by subclasses). */
    public abstract void process(WebSocketSession sourceConnection);

    /**
     * Helper class dealing with the leading and trailing braces defined for uuid representations
     * in the ccc protocol.
     */
    protected UUID uidFromString(String uid){
        if(uid == null)
            return null;

        if(uid.startsWith("{"))
            uid = uid.substring(1);

        if (uid.endsWith("}")) {
            uid = uid.substring(0, uid.length() - 1);
        }
        return UUID.fromString(uid);
    }

    /**
     * Helper method to avoid code duplication between the ConnectApp and ConnectGis message.
     */
    protected static void addClient(UUID sessionUid, boolean isAppConnection, String clientName, String apiVersion, WebSocketSession sourceConnection) {
        SockConnection con = new SockConnection(clientName, apiVersion, sourceConnection);
        Session s = Sessions.findBySessionUid(sessionUid);
        if(s == null){
            Session newSes = new Session(sessionUid, con, isAppConnection);
            Sessions.addOrReplace(newSes);
        }
        else if(s.getAppWebSocket() == null){
            boolean inTime = s.tryToAddSecondConnection(con, isAppConnection);
            if(!inTime)
                throw new RuntimeException("Second connection not added as time window for handshake is closed");
        }
        else{ // Connection already exists
            throw new RuntimeException("Second connection not added as it already exists in session");
        }
    }
}
