package ch.so.agi.service.message;

import ch.so.agi.service.exception.ForbiddenReconnectException;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import ch.so.agi.service.session.SockConnection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

/**
 * Message sent from app or gis to reconnect after an unexpectedly closed connection
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Reconnect extends Message {
    private static final Logger log = LoggerFactory.getLogger(Reconnect.class);

    @JsonProperty()
    @Nonnull
    private String oldConnectionKey;

    @JsonProperty()
    @Nonnull
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

        String errStringTemplate = """
                {
                    "method": "notifyError",
                    "code": 400,
                    "message": "Given key '%s' for the reconnect is invalid",
                }
                """;

        Message err = Message.forJsonString(String.format(errStringTemplate, oldConnectionKey));
        err.process(sourceConnection);
    }
}

