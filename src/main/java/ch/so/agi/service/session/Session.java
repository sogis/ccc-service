package ch.so.agi.service.session;

import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents one bidirectional route via the two websocket
 * connections app - server and gis - server.
 * The session connects one app and one gis instance, typically
 * both running on the same machine for one user.
 */
public class Session {
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
    private Connection appConnection;

    /**
     * Websocket connection server - gis
     */
    private Connection gisConnection;

    private static final Map<WebSocketSession, Session> sessionsBySocket = new ConcurrentHashMap<>();

    public Connection getAppConnection() {
        return appConnection;
    }

    public void setAppConnection(Connection appConnection) {
        updateSocketMapping(this.appConnection, false);
        this.appConnection = appConnection;
        updateSocketMapping(appConnection, true);
    }

    public Connection getGisConnection() {
        return gisConnection;
    }

    public void setGisConnection(Connection gisConnection) {
        updateSocketMapping(this.gisConnection, false);
        this.gisConnection = gisConnection;
        updateSocketMapping(gisConnection, true);
    }

    private void updateSocketMapping(Connection connection, boolean add) {
        if (connection == null) {
            return;
        }
        WebSocketSession socket = connection.getSocketConnection();
        if (socket == null) {
            return;
        }
        if (add) {
            sessionsBySocket.put(socket, this);
        } else {
            sessionsBySocket.remove(socket);
        }
    }

    public static Session findByConnection(WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return null;
        }
        return sessionsBySocket.get(webSocketSession);
    }
}
