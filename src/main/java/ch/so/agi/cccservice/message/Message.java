package ch.so.agi.cccservice.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.so.agi.cccservice.exception.MessageMalformedException;
import ch.so.agi.cccservice.session.SockConnection;
import ch.so.agi.cccservice.exception.MessageUnknownException;
import ch.so.agi.cccservice.message.app.CancelEditGeoObject;
import ch.so.agi.cccservice.message.app.ChangeLayerVisibility;
import ch.so.agi.cccservice.message.app.ConnectApp;
import ch.so.agi.cccservice.message.app.CreateGeoObject;
import ch.so.agi.cccservice.message.app.EditGeoObject;
import ch.so.agi.cccservice.message.app.ObjectUpdated;
import ch.so.agi.cccservice.message.app.DisconnectApp;
import ch.so.agi.cccservice.message.app.ReconnectApp;
import ch.so.agi.cccservice.message.app.ShowGeoObject;
import ch.so.agi.cccservice.message.gis.ConnectGis;
import ch.so.agi.cccservice.message.gis.DisconnectGis;
import ch.so.agi.cccservice.message.gis.EditGeoObjectDone;
import ch.so.agi.cccservice.message.gis.GeoObjectSelected;
import ch.so.agi.cccservice.message.gis.ReconnectGis;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;

/**
 * Root of all classes that implement ccc-messages.
 */
abstract public class Message {

    protected Message(String messageType) {
        this.messageType = messageType;
    }

    protected static final String GIS_CLIENT_TYPENAME = "gis";
    protected static final String APP_CLIENT_TYPENAME = "app";

    @NotNull
    private final String messageType;

    /**
     * The raw message as received through the websocket connection.
     * Provided as convenience to avoid having to deserialize and
     * serialize for messages just passing through the server.
     */
    private String rawMessage;

    protected static final Validator validator;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    static {
        try(ValidatorFactory factory = Validation.buildDefaultValidatorFactory()){
            validator = factory.getValidator();
        }
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public String getMessageType() { return messageType; }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Registry of all message types by their "method" name.
     * (Can easily be extended as new message types are added.)
     */
    private static final Map<String, Class<? extends Message>> MESSAGE_TYPES = new HashMap<>();
    static {
        MESSAGE_TYPES.put(ChangeLayerVisibility.MESSAGE_TYPE, ChangeLayerVisibility.class);
        MESSAGE_TYPES.put(ConnectApp.MESSAGE_TYPE, ConnectApp.class);
        MESSAGE_TYPES.put(ConnectGis.MESSAGE_TYPE, ConnectGis.class);
        MESSAGE_TYPES.put(ErrorMessage.MESSAGE_TYPE, ErrorMessage.class);
        MESSAGE_TYPES.put(EditGeoObjectDone.MESSAGE_TYPE, EditGeoObjectDone.class);
        MESSAGE_TYPES.put(CancelEditGeoObject.MESSAGE_TYPE, CancelEditGeoObject.class);
        MESSAGE_TYPES.put(CreateGeoObject.MESSAGE_TYPE, CreateGeoObject.class);
        MESSAGE_TYPES.put(EditGeoObject.MESSAGE_TYPE, EditGeoObject.class);
        MESSAGE_TYPES.put(ShowGeoObject.MESSAGE_TYPE, ShowGeoObject.class);
        MESSAGE_TYPES.put(GeoObjectSelected.MESSAGE_TYPE, GeoObjectSelected.class);
        MESSAGE_TYPES.put(ObjectUpdated.MESSAGE_TYPE, ObjectUpdated.class);
        MESSAGE_TYPES.put(ReconnectGis.MESSAGE_TYPE, ReconnectGis.class);
        MESSAGE_TYPES.put(ReconnectApp.MESSAGE_TYPE, ReconnectApp.class);
        MESSAGE_TYPES.put(DisconnectApp.MESSAGE_TYPE, DisconnectApp.class);
        MESSAGE_TYPES.put(DisconnectGis.MESSAGE_TYPE, DisconnectGis.class);
    }

    /**
     * Returns a new Message-Instance for the given json string.
     */
    public static Message forJsonString(String json) {

        try{
            // First parse the method field to decide which subclass to use
            JsonNode root = MAPPER.readTree(json);
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
            Message m = MAPPER.readValue(json, targetType);

            Set<ConstraintViolation<Message>> violations = validator.validate(m);
            if(!violations.isEmpty())
                throw new ConstraintViolationException(violations);

            return m;
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
     * Returns the raw message with apiVersion stripped for legacy v1.0 app clients.
     */
    protected String rawMessageForApp(SockConnection appCon) {
        if (!SockConnection.PROTOCOL_V1.equals(appCon.getApiVersion())) {
            return getRawMessage();
        }
        try {
            ObjectNode node = (ObjectNode) MAPPER.readTree(getRawMessage());
            node.remove("apiVersion");
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}