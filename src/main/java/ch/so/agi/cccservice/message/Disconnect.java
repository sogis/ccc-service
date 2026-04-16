package ch.so.agi.cccservice.message;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;

/**
 * Message sent from app or gis to signal an intentional, final disconnection.
 * Upon receiving this message, the server closes both connections immediately
 * and removes the session — no reconnect grace period is applied.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Disconnect extends Message {

    public Disconnect(String messageType) {
        super(messageType);
    }

    /**
     * Type of the disconnecting client (app or gis). To be implemented in subclasses.
     */
    protected abstract String clientType();

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = Sessions.findByConnection(sourceConnection);
        if (s == null) {
            log.warn("Ignoring {} from unknown connection", getMessageType());
            return;
        }

        if (s.tryInitiateTermination()) {
            log.info("Session {}: {} client sent disconnect — terminating session",
                    s.getSessionNr(), clientType());
            s.closeConnections(CloseStatus.NORMAL, "Client disconnected");
            Sessions.removeSession(s);
        }
    }
}
