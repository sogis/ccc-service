package ch.so.agi.service.message;

import ch.so.agi.service.exception.ConnectionRepeatException;
import ch.so.agi.service.exception.HandshakeToLateException;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import ch.so.agi.service.session.SockConnection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.socket.WebSocketSession;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
abstract public class Connect extends Message {

    public Connect(String messageType){ super(messageType); }

    @NotNull
    private UUID sessionUid;
    @JsonProperty()
    @NotNull
    private String clientName;
    @JsonProperty()
    @NotNull
    private String apiVersion;

    @JsonProperty("session") // needed to call uidFromString(...)
    private void setSessionUidFromString(String session){
        this.sessionUid = uidFromString(session);
    }

    public String getClientName() {
        return clientName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public UUID getSessionUid() { return sessionUid; }

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = addClient(sourceConnection);

        if(s.getPeerConnection(sourceConnection) != null){
            SessionReady.send(s.getAppWebSocket());
            SessionReady.send(s.getGisWebSocket());

            log.info("Session {}: Handshake finalized by {} client '{}' using protocol version {}", s.getSessionNr(), clientType(), getClientName(), getApiVersion());
        }
        else{
            log.info("Session {}: Handshake initialized by {} client '{}' using protocol version {}", s.getSessionNr(), clientType(), getClientName(), getApiVersion());
        }
    }

    /**
     * Type of the connecting client (app or gis). To be implemented in the subclasses
     */
    protected abstract String clientType();

    protected boolean isAppClient(){
        return APP_CLIENT_TYPENAME.equals(clientType());
    }

    private Session addClient(WebSocketSession sourceConnection) {

        SockConnection con = new SockConnection(clientName, apiVersion, sourceConnection);
        Session s = Sessions.findBySessionUid(sessionUid);
        if(s == null){
            s = new Session(sessionUid, con, isAppClient());
            Sessions.addOrReplace(s);
        }
        else {
            boolean inTime = s.tryToAddSecondConnection(con, isAppClient());
            if(!inTime)
                throw new HandshakeToLateException("Connect could not be executed as time window for handshake is closed");

            Sessions.addOrReplace(s);
        }
        return s;
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
}
