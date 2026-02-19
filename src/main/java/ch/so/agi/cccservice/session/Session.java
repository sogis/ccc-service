package ch.so.agi.cccservice.session;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import ch.so.agi.cccservice.exception.HandshakeIncompleteException;
import ch.so.agi.cccservice.exception.ReceivingConnectionClosedException;
import ch.so.agi.cccservice.message.Message;

/**
 * Represents one bidirectional route via the two websocket
 * connections app - server and gis - server.
 * The session connects one app and one gis instance, typically
 * both running on the same machine for one user.
 */
public class Session implements Comparable<Session>{
    private static final int HANDSHAKE_MAXDURATION_MILLIS = 60 * 1000;
    private static final Logger log = LoggerFactory.getLogger(Session.class);
    /**
     * Maximum delay accepted between start and finish of the handshake
     */
    private Duration handShakeMaxDuration;
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

    /**
     * Timestamp when the app connection was first detected as closed.
     * Used to implement a grace period for reconnection attempts.
     */
    private LocalDateTime appConnectionClosedAt;

    /**
     * Timestamp when the gis connection was first detected as closed.
     * Used to implement a grace period for reconnection attempts.
     */
    private LocalDateTime gisConnectionClosedAt;

    /**
     * Guards against concurrent termination attempts (e.g. both WebSocket connections
     * closing simultaneously). Only the first thread to set this wins and performs cleanup.
     */
    private final AtomicBoolean terminationStarted = new AtomicBoolean(false);

    public SockConnection getAppConnection() {
        return appConnection;
    }

    public SockConnection getGisConnection() {
        return gisConnection;
    }

    public SockConnection getPeerConnection(WebSocketSession con) {
        if(appConnection == null || gisConnection == null)
            return null;

        if(con.equals(getAppWebSocket()))
            return gisConnection;
        else
            return appConnection;
    }

    /**
     * Checks if the given WebSocket connection uses protocol V1.0.
     * Returns false if the connection is not found in this session.
     */
    public boolean isV10Connection(WebSocketSession ws) {
        if (appConnection != null && ws.equals(getAppWebSocket())) {
            return SockConnection.PROTOCOL_V1.equals(appConnection.getApiVersion());
        }
        if (gisConnection != null && ws.equals(getGisWebSocket())) {
            return SockConnection.PROTOCOL_V1.equals(gisConnection.getApiVersion());
        }
        return false;
    }

    /**
     * Opens the Session and the handshake as reaction to either the connectApp or connectGIS message.
     */
    public Session(UUID sessionUid, SockConnection connection, boolean isAppConnection){
        this.sessionUid = sessionUid;
        if(isAppConnection)
            this.appConnection = connection;
        else
            this.gisConnection = connection;
        this.sessionNr = Sessions.getNextSessionNr();
        this.handShakeInitialized = LocalDateTime.now(ZoneId.systemDefault());
        this.handShakeMaxDuration = Duration.ofMillis(HANDSHAKE_MAXDURATION_MILLIS);
    }

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

    public int getSessionNr() {
        return sessionNr;
    }

    /**
     * Adds the second connection (app or gis) to the session, given that the time window for the
     * handshake is still open.
     * Returns true if added, false if adding failed due to closed handshake window.
     */
    public boolean tryToAddSecondConnection(SockConnection con, boolean isAppConnection){
        if(handShakeInitialized.plus(handShakeMaxDuration).isBefore(LocalDateTime.now(ZoneId.systemDefault())))
            return false;

        if(isAppConnection){
            appConnection = con;
        }
        else {
            gisConnection = con;
        }
        return true;
    }

    public void assertConnected(){
        assertConnected(null);
    }

    public void assertConnected(Message message){

        if(gisConnection == null){
            throw new HandshakeIncompleteException("Handshake is incomplete as connection to gis is null.");
        }
        else if(appConnection == null){
            throw new HandshakeIncompleteException("Handshake is incomplete as connection to app is null.");
        }

        if(!gisConnection.isOpen()){
            throw new ReceivingConnectionClosedException("Can not forward message as gis connection is closed");
        }
        else if(!appConnection.isOpen()){
            throw new ReceivingConnectionClosedException("Can not forward message as app connection is closed");
        }
    }

    /**
     * Setter for the unit tests
     */
    public void setHandShakeMaxDuration(Duration d){
        this.handShakeMaxDuration = d;
    }



    public boolean hasClosedConnections() {
        boolean gisClosed = false;
        boolean appClosed = false;

        if(gisConnection != null)
            gisClosed = !getGisWebSocket().isOpen();

        if(appConnection != null)
            appClosed = !getAppWebSocket().isOpen();

        return gisClosed || appClosed;
    }

    /**
     * Marks the connection as closed by recording the current timestamp.
     * Called immediately from the WebSocket afterConnectionClosed callback.
     */
    public void markConnectionClosed(WebSocketSession ws) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        if (appConnection != null && ws.equals(getAppWebSocket())) {
            appConnectionClosedAt = now;
        }
        if (gisConnection != null && ws.equals(getGisWebSocket())) {
            gisConnectionClosedAt = now;
        }
    }

    /**
     * Resets the connection closed timestamp after a successful reconnect.
     */
    public void clearConnectionClosedAt(boolean isAppConnection) {
        if (isAppConnection) {
            appConnectionClosedAt = null;
        } else {
            gisConnectionClosedAt = null;
        }
    }

    /**
     * Checks if the session has closed connections that should be considered stale.
     * For V1.0 sessions: immediately stale when any connection is closed (no reconnect support).
     * For V1.2 sessions: stale only after the grace period has elapsed, allowing time for reconnection.
     */
    public boolean hasStaleClosedConnections(Duration gracePeriod) {
        if (!hasV12Connection()) {
            return hasClosedConnections();
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        boolean appStale = appConnectionClosedAt != null && now.isAfter(appConnectionClosedAt.plus(gracePeriod));
        boolean gisStale = gisConnectionClosedAt != null && now.isAfter(gisConnectionClosedAt.plus(gracePeriod));

        return appStale || gisStale;
    }

    public boolean handShakeExceeded() {
        boolean handshakeComplete = appConnection != null && gisConnection != null;
        if (handshakeComplete) return false;
        return LocalDateTime.now(ZoneId.systemDefault()).isAfter(handShakeInitialized.plus(handShakeMaxDuration));
    }

    public boolean hasV12Connection(){
        return !v12Connections().isEmpty();
    }

    public List<SockConnection> v12Connections(){
        List<SockConnection> connections = new ArrayList<>();

        if(gisConnection != null &&  SockConnection.PROTOCOL_V12.equals(gisConnection.getApiVersion()))
            connections.add(gisConnection);

        if(appConnection != null &&  SockConnection.PROTOCOL_V12.equals(appConnection.getApiVersion()))
            connections.add(appConnection);

        return connections;
    }

    /**
     * Initiates session termination exactly once, even if called from multiple threads.
     * Returns true if this call triggered the termination, false if another thread was first.
     */
    public boolean tryInitiateTermination() {
        return terminationStarted.compareAndSet(false, true);
    }

    /**
     * Closes both connections without specifying a reason.
     * Use closeConnections(CloseStatus) to provide a reason to clients.
     */
    public void closeConnections() {
        closeConnections(CloseStatus.NORMAL, null);
    }

    /**
     * Closes both GIS and App connections with the specified close status.
     * The close status is communicated to the clients.
     */
    public void closeConnections(CloseStatus closeStatus) {
        closeConnections(closeStatus, null);
    }

    /**
     * Closes both GIS and App connections with the specified close status and reason.
     * The close status and reason are communicated to the clients.
     *
     * @param closeStatus the close status code
     * @param reason optional reason text (max 123 bytes UTF-8, per WebSocket spec)
     */
    public void closeConnections(CloseStatus closeStatus, String reason) {
        CloseStatus statusWithReason = reason != null
            ? new CloseStatus(closeStatus.getCode(), reason)
            : closeStatus;

        if(gisConnection != null){
            try {
                getGisWebSocket().close(statusWithReason);
            } catch (IOException | IllegalStateException e) {
                log.warn("Session {}: Exception while closing gis connection: {}", getSessionNr(), e.toString());
            }
        }

        if(appConnection != null){
            try {
                getAppWebSocket().close(statusWithReason);
            } catch (IOException | IllegalStateException e) {
                log.warn("Session {}: Exception while closing app connection: {}", getSessionNr(), e.toString());
            }
        }
    }

    @Override
    public int compareTo(Session o) {
        return Integer.compare(this.getSessionNr(), o.getSessionNr());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session s)) return false;
        return sessionNr == s.sessionNr;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(sessionNr);
    }
}
