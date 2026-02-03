package ch.so.agi.cccservice.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.so.agi.cccservice.exception.ForbiddenReconnectException;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;
import jakarta.validation.constraints.NotNull;

/**
 * Message sent from app or gis to reconnect after an unexpectedly closed connection
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Reconnect extends Message {
    private static final Logger log = LoggerFactory.getLogger(Reconnect.class);

    @JsonProperty()
    @NotNull
    private String oldConnectionKey;

    @JsonProperty()
    @NotNull
    private int oldSessionNumber;

    public Reconnect(String methodType) {
        super(methodType);
    }

    /**
     * Type of the connecting client (app or gis). To be implemented in the subclasses
     */
    protected abstract String clientType();

    protected boolean isAppClient(){
        return APP_CLIENT_TYPENAME.equals(clientType());
    }

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = Sessions.findByConnectionKey(oldConnectionKey, isAppClient());
        if(s == null){
            sendErrorMessage(sourceConnection, clientType());
            return;
        }

        s.assertConnected();

        SockConnection con = s.getAppConnection();
        if(!isAppClient())
            con = s.getGisConnection();

        if(SockConnection.PROTOCOL_V1.equals(con.getApiVersion()))
            throw new ForbiddenReconnectException("V1 clients are not allowed to reconnect");

        con.switchToNewWebSocketCon(sourceConnection);

        log.info("Session {}: {} reconnected.", s.getSessionNr(), clientType());
    }

    private void sendErrorMessage(WebSocketSession sourceConnection, String clientName) {
        log.warn("Session {}: {} tried to reconnect, but given key '{}' is invalid.", oldSessionNumber, clientName, oldConnectionKey);

        String errMessage = "{\n"
                + "    \"method\": \"notifyError\",\n"
                + "    \"code\": 400,\n"
                + "    \"message\": \"Given key '" + oldConnectionKey + "' for the reconnect is invalid\"\n"
                + "}";

        try {
            sourceConnection.sendMessage(new TextMessage(errMessage));
        } catch (Exception e) {
            log.error("Failed to send reconnect error to {}: {}", clientName, e.toString());
        }
    }
}

