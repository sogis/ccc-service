package ch.so.agi.cccservice.session;

import ch.so.agi.cccservice.exception.HandshakeIncompleteException;
import ch.so.agi.cccservice.exception.ReceivingConnectionClosedException;
import ch.so.agi.cccservice.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

/**
 * Represents one bidirectional route via the two websocket
 * connections app - server and gis - server.
 * The session connects one app and one gis instance, typically
 * both running on the same machine for one user.
 */
public class Session implements Comparable{
    private static final int HANDSHAKE_MAXDURATION_MILLIS = 60 * 1000;
    private static final Logger log = LoggerFactory.getLogger(Session.class);
    /**
     * Maximum delay accepted between start and finish of the handshake
     */
    private Duration handShakeMaxDuration;
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
     * Opens the Session and the handshake as reaction to either the connectApp or connectGIS message.
     */
    public Session(UUID sessionUid, SockConnection connection, boolean isAppConnection){
        this.sessionUid = sessionUid;
        if(isAppConnection)
            this.appConnection = connection;
        else
            this.gisConnection = connection;
        this.sessionNr = getNextSessionNr();
        this.handShakeInitialized = LocalDateTime.now();
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

    private synchronized int getNextSessionNr(){
        lastSessionNr++;
        return lastSessionNr;
    }

    public boolean hasClosedConnections() {
        boolean gisClosed = false;
        boolean appClosed = false;

        if(gisConnection != null)
            gisClosed = !getGisWebSocket().isOpen();

        if(appConnection != null)
            appClosed = !getAppConnection().isOpen();

        return gisClosed || appClosed;
    }

    public boolean handShakeExceeded() {
        return LocalDateTime.now().isAfter(handShakeInitialized.plus(handShakeMaxDuration));
    }

    public boolean hasV2Connection(){
        return !( v2Connections().isEmpty() );
    }

    public List<SockConnection> v2Connections(){
        List<SockConnection> connections = new Vector<>();

        if(gisConnection != null &&  SockConnection.PROTOCOL_V12.equals(gisConnection.getApiVersion()))
            connections.add(gisConnection);

        if(appConnection != null &&  SockConnection.PROTOCOL_V12.equals(appConnection.getApiVersion()))
            connections.add(appConnection);

        return connections;
    }

    public void closeConnections() {
        if(gisConnection != null){
            try {
                getGisWebSocket().close();
            } catch (IOException e) {
                log.error("Session {}: Exception was thrown while closing gis connection. Exception: {}", getSessionNr(), e.toString());
            }
        }

        if(appConnection != null){
            try {
                getAppWebSocket().close();
            } catch (IOException e) {
                log.error("Session {}: Exception was thrown while closing app connection. Exception: {}", getSessionNr(), e.toString());
            }
        }
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(this.getSessionNr(), ((Session) o).getSessionNr());
    }
}
