package ch.so.agi.service.session;

import org.springframework.web.socket.WebSocketSession;

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
    private final int handShakeMaxDuration;
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

    public Session(UUID sessionUid, SockConnection connection, boolean isAppConnection){
        this(sessionUid, connection, isAppConnection, 60);
    }

    protected Session(UUID sessionUid, SockConnection connection, boolean isAppConnection, int handShakeMaxDuration){
        this.sessionUid = sessionUid;
        if(isAppConnection)
            this.appConnection = connection;
        else
            this.gisConnection = connection;
        this.sessionNr = getNextSessionNr();
        this.handShakeInitialized = LocalDateTime.now();
        this.handShakeMaxDuration = handShakeMaxDuration;
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

    public boolean tryToAddSecondConnection(SockConnection con, boolean isAppConnection){
        if(handShakeInitialized.plusSeconds(handShakeMaxDuration).isBefore(LocalDateTime.now()))
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
