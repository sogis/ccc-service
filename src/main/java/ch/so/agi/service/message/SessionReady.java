package ch.so.agi.service.message;

import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import ch.so.agi.service.session.SockConnection;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Message sent from the ccc-server to the domain-application and GIS to notify the successful pairing between them.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionReady {

    public static final String METHOD_TYPE = "notifySessionReady";
    private static final Logger log = LoggerFactory.getLogger(SessionReady.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String method = METHOD_TYPE; // Constant
    @NotNull
    private String apiVersion;
    private String connectionKey;
    private Integer sessionNr;

    private SessionReady(){}

    public static void send(WebSocketSession receiver){
        SessionReady msg = new SessionReady();
        msg.sendImpl(receiver);
    }

    private void sendImpl(WebSocketSession receiver){
        Session s = Sessions.findByConnection(receiver);
        s.assertConnected();

        SockConnection con = s.getAppConnection();
        String clientType = Message.APP_CLIENT_TYPENAME;
        if(!receiver.equals(con.getWebSocketConnection())){
            con = s.getGisConnection();
            clientType = Message.GIS_CLIENT_TYPENAME;
        }

        this.apiVersion = con.getApiVersion();
        if(con.getApiVersion().equals(SockConnection.PROTOCOL_V2)){
            this.connectionKey = con.getConnectionKey();
            this.sessionNr = s.getSessionNr();
        }

        String json = null;
        try {
            json = MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        con.sendMessage(json);
        log.info("Session {}: Sent session ready to {} using protocol version {}.", s.getSessionNr(), clientType, apiVersion);
        log.debug("Sent json: '{}'.", json);
    }
}
