package ch.so.agi.cccservice.message;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.so.agi.cccservice.exception.MessageUnknownException;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;

/**
 * Message sent from a V1.2 app or gis client to signal an intentional, final disconnection.
 * Upon receiving this message, the server closes both connections immediately
 * and removes the session — no reconnect grace period is applied.
 * Only valid for V1.2 connections; V1.0 connections are terminated immediately
 * on close and do not need this message.
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

    protected abstract boolean isAppClient();

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = Sessions.findByConnection(sourceConnection);
        if (s == null) {
            log.warn("Ignoring {} from unknown connection", getMessageType());
            return;
        }

        if (!s.hasV12Connection()) {
            throw new MessageUnknownException("Disconnect is only supported for V1.2 connections");
        }

        String actualType = s.clientType(sourceConnection);
        if (!clientType().equals(actualType)) {
            log.warn("Session {}: {} sent {} — ignoring (wrong client type)",
                    s.getSessionNr(), actualType, getMessageType());
            return;
        }

        if (s.tryInitiateTermination()) {
            log.info("Session {}: {} client sent disconnect — terminating session",
                    s.getSessionNr(), clientType());
            s.closeConnections(CloseStatus.NORMAL, clientType() + " sent disconnect");
            Sessions.removeSession(s);
        }
    }
}
