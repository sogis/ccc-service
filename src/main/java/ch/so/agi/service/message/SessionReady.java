package ch.so.agi.service.message;

import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.socket.WebSocketSession;

/**
 * Message sent from the ccc-server to the domain-application and GIS to notify the successful pairing between them.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionReady extends Message {

    public static final String MESSAGE_TYPE = "notifySessionReady";

    @JsonProperty("apiVersion")
    private String apiVersion;

    public SessionReady() {
        super(MESSAGE_TYPE);
    }

    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = Sessions.findByConnection(sourceConnection);
        s.assertConnected(this);
        if (s.getAppWebSocket() != null) {
            s.getAppConnection().sendMessage(getRawMessage());
        }
        if (s.getGisWebSocket() != null) {
            s.getGisConnection().sendMessage(getRawMessage());
        }
        log.info("Session {}: Notify session ready for protocol {}", s.getSessionNr(), getApiVersion());
    }
}
