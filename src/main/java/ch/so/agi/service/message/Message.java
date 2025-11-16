package ch.so.agi.service.message;

import ch.so.agi.service.exception.ConnectionRepeatException;
import ch.so.agi.service.exception.HandshakeIncompleteException;
import ch.so.agi.service.exception.HandshakeToLateException;
import ch.so.agi.service.exception.MessageMalformedException;
import ch.so.agi.service.exception.MessageUnknownException;
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
            throw new MessageMalformedException(json, e);
        }

        JsonNode methodNode = root.get("method");
        if (methodNode == null || !methodNode.isTextual()) {
            throw new MessageMalformedException(json);
        }

        String method = methodNode.asText();
        Class<? extends Message> targetType = MESSAGE_TYPES.get(method);

        if (targetType == null) {
            throw new MessageUnknownException(method);
        }

        try {
            return mapper.readValue(json, targetType);
        } catch (Exception e) {
            throw new MessageMalformedException(json, e);
        }
    }

    /**
     * Processes the Message (to be implemented by subclasses).
     */
    public abstract void process(WebSocketSession sourceConnection);

    /**
     * Builds a short human readable description of the provided message
     * containing at least its concrete type and raw payload.
     */
    public static String describe(Message message) {
        if (message == null) {
            return "<unknown message>";
        }

        String type = message.getClass().getSimpleName();
        String payload = message.getRawMessage();
        if (payload == null || payload.isBlank()) {
            return type;
        }
        return String.format("%s payload=%s", type, payload);
    }

    protected final String describeMessage() {
        return describe(this);
    }

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
    protected Session addClient(UUID sessionUid, boolean isAppConnection, String clientName, String apiVersion, WebSocketSession sourceConnection) {
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
                log.warn("Session {}: {} tried to connect {} client '{}' while it is already connected.",
                        sessionUid == null ? "<unknown>" : sessionUid,
                        describeMessage(),
                        isAppConnection ? "app" : "gis",
                        clientName);
                throw new ConnectionRepeatException(this, sessionUid, isAppConnection);
            }

            boolean inTime = s.tryToAddSecondConnection(con, isAppConnection);
            if(!inTime) {
                log.warn("Session {}: {} tried to connect {} client '{}' after the handshake window closed.",
                        sessionUid == null ? "<unknown>" : sessionUid,
                        describeMessage(),
                        isAppConnection ? "app" : "gis",
                        clientName);
                throw new HandshakeToLateException(this, sessionUid);
            }

            Sessions.addOrReplace(s);
        }
        return s;
    }

    protected Session requireSession(WebSocketSession sourceConnection) {
        String connectionId = sourceConnection == null ? "<unknown>" : sourceConnection.getId();
        Session session = sourceConnection == null ? null : Sessions.findByConnection(sourceConnection);
        if (session == null) {
            log.warn("{} can not be processed because connection {} is not associated with a session.", describeMessage(), connectionId);
            throw new HandshakeIncompleteException(this, "No session available for connection " + connectionId);
        }
        return session;
    }
}
