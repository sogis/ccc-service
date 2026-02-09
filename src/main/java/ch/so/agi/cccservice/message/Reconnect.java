package ch.so.agi.cccservice.message;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.so.agi.cccservice.exception.ForbiddenReconnectException;
import ch.so.agi.cccservice.exception.RateLimitExceededException;
import ch.so.agi.cccservice.security.ConnectionRateLimiter;
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

    @JsonProperty
    @NotNull
    private String oldConnectionKey;

    @JsonProperty
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
        String clientIp = extractClientIp(sourceConnection);
        ConnectionRateLimiter rateLimiter = ConnectionRateLimiter.getReconnectLimiter();

        // Check rate limit before processing
        if (!rateLimiter.isAllowed(clientIp)) {
            throw new RateLimitExceededException(
                    "Too many failed reconnect attempts. Please wait before retrying.");
        }

        Session s = Sessions.findByConnectionKey(oldConnectionKey, isAppClient());
        if(s == null){
            rateLimiter.recordFailedAttempt(clientIp);
            log.warn("Reconnect failed for {} (session {}): No session found for given key. Key may have expired or session was removed.",
                    clientType(), oldSessionNumber);
            log.debug("Reconnect failed - invalid key details: key='{}', clientType={}", oldConnectionKey, clientType());
            sendErrorMessage(sourceConnection, clientType());
            return;
        }
        if(s.getSessionNr() != oldSessionNumber){
            rateLimiter.recordFailedAttempt(clientIp);
            log.warn("Reconnect failed for {}: Session number mismatch. Client sent {}, but key belongs to different session.",
                    clientType(), oldSessionNumber);
            log.debug("Reconnect session mismatch details: clientSession={}, actualSession={}, key='{}'",
                    oldSessionNumber, s.getSessionNr(), oldConnectionKey);
            sendErrorMessage(sourceConnection, clientType());
            return;
        }

        SockConnection con = isAppClient() ? s.getAppConnection() : s.getGisConnection();

        if(SockConnection.PROTOCOL_V1.equals(con.getApiVersion()))
            throw new ForbiddenReconnectException("V1 clients are not allowed to reconnect");

        // 1. Switch to new WebSocket first
        con.switchToNewWebSocketCon(sourceConnection);

        // 2. Update Sessions map so the new WebSocket is indexed
        Sessions.addOrReplace(s);

        // 3. Assert both connections are open (including the new WebSocket)
        s.assertConnected();

        // 4. Send keyChange so client has a valid key for the next reconnect
        KeyChange.sendKeyChangeToConnection(con);

        // 5. Record successful reconnect to reset rate limit
        rateLimiter.recordSuccess(clientIp);

        log.info("Session {}: {} reconnected.", s.getSessionNr(), clientType());
    }

    private String extractClientIp(WebSocketSession connection) {
        InetSocketAddress remoteAddress = connection.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }

    private void sendErrorMessage(WebSocketSession sourceConnection, String clientName) {
        String errMessage = "{\n"
                + "    \"method\": \"" + ErrorMessage.MESSAGE_TYPE + "\",\n"
                + "    \"code\": 400,\n"
                + "    \"message\": \"Reconnect failed: invalid or expired connection key\"\n"
                + "}";

        try {
            sourceConnection.sendMessage(new TextMessage(errMessage));
        } catch (Exception e) {
            log.error("Failed to send reconnect error to {}: {}", clientName, e.toString());
        }
    }
}

