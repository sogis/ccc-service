package ch.so.agi.service.message;

import ch.so.agi.service.exception.ConnectionRepeatException;
import ch.so.agi.service.exception.HandshakeToLateException;
import ch.so.agi.service.exception.MessageMalformedException;
import ch.so.agi.service.exception.MessageUnknownException;
import ch.so.agi.service.message.app.CancelEditGeoObject;
import ch.so.agi.service.message.app.ChangeLayerVisibility;
import ch.so.agi.service.message.app.ConnectApp;
import ch.so.agi.service.message.app.CreateGeoObject;
import ch.so.agi.service.message.app.EditGeoObject;
import ch.so.agi.service.message.app.ObjectUpdated;
import ch.so.agi.service.message.gis.ConnectGis;
import ch.so.agi.service.message.gis.EditGeoObjectDone;
import ch.so.agi.service.message.gis.GeoObjectSelected;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import ch.so.agi.service.session.SockConnection;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    protected Message(String messageType) {
        this.messageType = messageType;
    }

    private final String messageType;

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

    public String getMessageType() { return messageType; }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Registry of all message types by their "method" name.
     * (Can easily be extended as new message types are added.)
     */
    private static final Map<String, Class<? extends Message>> MESSAGE_TYPES = new HashMap<>();
    static {
        MESSAGE_TYPES.put(ChangeLayerVisibility.MESSAGE_TYPE, ChangeLayerVisibility.class);
        MESSAGE_TYPES.put(ConnectApp.MESSAGE_TYPE, ConnectApp.class);
        MESSAGE_TYPES.put(ConnectGis.MESSAGE_TYPE, ConnectGis.class);
        MESSAGE_TYPES.put(Error.MESSAGE_TYPE, Error.class);
        MESSAGE_TYPES.put(EditGeoObjectDone.MESSAGE_TYPE, EditGeoObjectDone.class);
        MESSAGE_TYPES.put(CancelEditGeoObject.MESSAGE_TYPE, CancelEditGeoObject.class);
        MESSAGE_TYPES.put(CreateGeoObject.MESSAGE_TYPE, CreateGeoObject.class);
        MESSAGE_TYPES.put(EditGeoObject.MESSAGE_TYPE, EditGeoObject.class);
        MESSAGE_TYPES.put(GeoObjectSelected.MESSAGE_TYPE, GeoObjectSelected.class);
        MESSAGE_TYPES.put(ObjectUpdated.MESSAGE_TYPE, ObjectUpdated.class);
        MESSAGE_TYPES.put(SessionReady.MESSAGE_TYPE, SessionReady.class);
    }

    /**
     * Returns a new Message-Instance for the given json string.
     */
    public static Message forJsonString(String json) {

        try{
            // First parse the method field to decide which subclass to use
            JsonNode root = mapper.readTree(json);
            JsonNode methodNode = root.get("method");
            if (methodNode == null || !methodNode.isTextual()) {
                throw new MessageMalformedException("Could not interpret message due to missing or malformed 'method' property. Message was: " + json);
            }

            String method = methodNode.asText();
            Class<? extends Message> targetType = MESSAGE_TYPES.getOrDefault(method, null);

            if (targetType == null) {
                throw new MessageUnknownException(String.format("Could not interpret message as method '%s' is not known.", method));
            }

            // Deserialize into the discovered subclass
            return mapper.readValue(json, targetType);
        }
        catch(JsonProcessingException e){
            throw new MessageMalformedException("Sent json is malformed or does not comply to the ccc specification. Message was: " + json, e);
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
        else if(Sessions.findByConnection(sourceConnection) != null) {
            throw new ConnectionRepeatException("Connect could not be executed as client is already connected");
        }
        else {
            boolean inTime = s.tryToAddSecondConnection(con, isAppConnection);
            if(!inTime)
                throw new HandshakeToLateException("Connect could not be executed as time window for handshake is closed");
        }
        return s;
    }
}