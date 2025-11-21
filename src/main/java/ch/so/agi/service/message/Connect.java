package ch.so.agi.service.message;

import ch.so.agi.service.exception.ConnectionRepeatException;
import ch.so.agi.service.exception.HandshakeToLateException;
import ch.so.agi.service.message.Message;
import ch.so.agi.service.message.gis.ConnectGis;
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
    public static final String SES_READY_METHOD = "notifySessionReady";

    private static final String READY_MESSAGE_V1 = """
            {
                "method": "notifySessionReady",
                "apiVersion": "%s",
            }
            """;

    private static final String READY_MESSAGE_V2 = """
            {
                "method": "notifySessionReady",
                "apiVersion": "%s",
                "connection_key": "%s",
                "session_nr": %s
            }
            """;

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
            sendSessionReady(s.getGisConnection(), s.getSessionNr());
            sendSessionReady(s.getAppConnection(), s.getSessionNr());

            log.info("Session {}: Handshake finalized by gis client '{}' using protocol version {}", s.getSessionNr(), getClientName(), getApiVersion());
        }
        else{
            log.info("Session {}: Handshake initialized by gis client '{}' using protocol version {}", s.getSessionNr(), getClientName(), getApiVersion());
        }
    }

    private Session addClient(WebSocketSession sourceConnection) {
        boolean isAppConnection = true;
        if(ConnectGis.MESSAGE_TYPE.equals(getMessageType()))
            isAppConnection = false;

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

    private void sendSessionReady(SockConnection con, int sessionNr){
        String readyMessage = null;
        if(SockConnection.PROTOCOL_V2.equals(con.getApiVersion())){
            readyMessage = READY_MESSAGE_V2;
            readyMessage = String.format(readyMessage, SockConnection.PROTOCOL_V2, con.getConnectionKey(), sessionNr);
        }
        else{ // V1
            readyMessage = READY_MESSAGE_V1;
            readyMessage = String.format(readyMessage, SockConnection.PROTOCOL_V1);
        }
        con.sendMessage(readyMessage);
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
