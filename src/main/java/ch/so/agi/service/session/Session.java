package ch.so.agi.service.session;

import org.springframework.web.socket.WebSocketSession;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents one bidirectional route via the two websocket
 * connections app - server and gis - server.
 * The session connects one app and one gis instance, typically
 * both running on the same machine for one user.
 */
public class Session {
    /**
     * Maximum delay accepted between start and finish of the handshake
     */
    private final Duration handShakeMaxDuration;
    /**
     * Counter to make sure each session gets a unique
     * number. Counts up from 0 as long as the server runs.
     */
    private static int lastSessionNr = 0;
    /**
     * The session uuid used in the http request App -> GIS
     * to link the corresponding app - server and GIS - server
     * connection.
     */
    private UUID sessionUid;
    /**
     * Unique and easily human readable number. To be displayed
     * in gui's and logs
     */
    private int sessionNr;
    /**
     * Time at which either the app or gis connection
     * initiated the handshake
     */
    private LocalDateTime handShakeInitialized;

    /**
     * Websocket connection server - app
     */
    private SockConnection appConnection;

    /**
     * Websocket connection server - gis
     */
    private SockConnection gisConnection;

    public static final Duration DEFAULT_HANDSHAKE_MAX_DURATION = Duration.ofSeconds(60);

    /**
     * Opens the Session and the handshake as reaction to either the connectApp or connectGIS message.
     */
    public Session(UUID sessionUid, SockConnection connection, boolean isAppConnection, Duration handShakeMaxDuration){
        this.sessionUid = sessionUid;
        if(isAppConnection)
            this.appConnection = connection;
        else
            this.gisConnection = connection;
        this.sessionNr = getNextSessionNr();
        this.handShakeInitialized = LocalDateTime.now();
        Duration duration = handShakeMaxDuration == null ? DEFAULT_HANDSHAKE_MAX_DURATION : handShakeMaxDuration;
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Handshake duration must be positive");
        }
        this.handShakeMaxDuration = duration;
    }

    /*
    public SockConnection getAppConnection() {
        return appConnection;
    }

    public SockConnection getGisConnection() {
        return gisConnection;
    }

     */
    public WebSocketSession getAppWebSocket(){
        if(appConnection == null)
            return null;

        return appConnection.getWebSocketConnection();
    }

    public WebSocketSession getGisWebSocket(){
        if(gisConnection == null)
            return null;

        return gisConnection.getWebSocketConnection();
    }

    public UUID getSessionUid() {
        return sessionUid;
    }

    /**
     * Adds the second connection (app or gis) to the session, given that the time window for the
     * handshake is still open.
     * Returns true if added, false if adding failed due to closed handshake window.
     */
    public boolean tryToAddSecondConnection(SockConnection con, boolean isAppConnection){
        if(handShakeInitialized.plus(handShakeMaxDuration).isBefore(LocalDateTime.now()))
            return false;

        if(isAppConnection){
            appConnection = con;
        }
        else {
            gisConnection = con;
        }
        return true;
    }

    private synchronized int getNextSessionNr(){
        lastSessionNr++;
        return lastSessionNr;
    }
}
