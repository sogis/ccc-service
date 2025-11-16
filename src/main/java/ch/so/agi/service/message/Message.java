package ch.so.agi.service.message;

import ch.so.agi.service.exception.ConnectionRepeat;
import ch.so.agi.service.exception.HandshakeIncomplete;
import ch.so.agi.service.exception.HandshakeToLate;
import ch.so.agi.service.exception.MessageMalformed;
import ch.so.agi.service.exception.MessageUnknown;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import ch.so.agi.service.session.SockConnection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * The raw message as received through the websocket connection.
     * Provided as convenience to avoid having to deserialize and
     * serialize for messages just passing through the server.
     */
    private String rawMessage;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    /**
     * Registry of all message types by their "method" name.
     * (Can easily be extended as new message types are added.)
     */
    private static final Map<String, Class<? extends Message>> MESSAGE_TYPES = new HashMap<>();
    static {
        MESSAGE_TYPES.put(ChangeLayerVisibility.MESSAGE_TYPE, ChangeLayerVisibility.class);
        MESSAGE_TYPES.put(ConnectApp.MESSAGE_TYPE, ConnectApp.class);
        MESSAGE_TYPES.put(ConnectGis.MESSAGE_TYPE, ConnectGis.class);
        MESSAGE_TYPES.put(NotifyError.MESSAGE_TYPE, NotifyError.class);
    }

    /**
     * Returns a new Message-Instance for the given json string.
     * Throws MessageMalformed if the json string is not understood or
     * MessageUnknown if the method is not registered.
     */
    public static Message forJsonString(String json) {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root;
        try {
            root = mapper.readTree(json);
        } catch (Exception e) {
            throw new MessageMalformed(json, e);
        }

        JsonNode methodNode = root.get("method");
        if (methodNode == null || !methodNode.isTextual()) {
            throw new MessageMalformed(json);
        }

        String method = methodNode.asText();
        Class<? extends Message> targetType = MESSAGE_TYPES.get(method);

        if (targetType == null) {
            throw new MessageUnknown(method);
        }

        try {
            return mapper.readValue(json, targetType);
        } catch (Exception e) {
            throw new MessageMalformed(json, e);
        }
    }

    /**
     * Processes the Message (to be implemented by subclasses).
     */
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
    protected static Session addClient(UUID sessionUid, boolean isAppConnection, String clientName, String apiVersion, WebSocketSession sourceConnection) {
        SockConnection con = new SockConnection(clientName, apiVersion, sourceConnection);
        Session s = Sessions.findBySessionUid(sessionUid);
        if(s == null){
            s = new Session(sessionUid, con, isAppConnection);
            Sessions.addOrReplace(s);
        }
        else {
            boolean isAppAlreadyConnected = s.getAppWebSocket() != null;
            boolean isGisAlreadyConnected = s.getGisWebSocket() != null;

            if ((isAppConnection && isAppAlreadyConnected) || (!isAppConnection && isGisAlreadyConnected)) {
                throw new ConnectionRepeat(sessionUid);
            }

            boolean inTime = s.tryToAddSecondConnection(con, isAppConnection);
            if(!inTime)
                throw new HandshakeToLate(sessionUid);

            Sessions.addOrReplace(s);
        }
        return s;
    }

    protected Session requireSession(WebSocketSession sourceConnection) {
        String connectionId = sourceConnection == null ? "<unknown>" : sourceConnection.getId();
        Session session = sourceConnection == null ? null : Sessions.findByConnection(sourceConnection);
        if (session == null) {
            throw new HandshakeIncomplete("No session available for connection " + connectionId);
        }
        return session;
    }
}
