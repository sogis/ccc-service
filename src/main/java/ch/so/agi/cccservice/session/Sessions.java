package ch.so.agi.cccservice.session;

import org.springframework.web.socket.WebSocketSession;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Sessions {
    /**
     * The central session collection of the ccc service.
     * Each session is contained twice in the collection, with
     * the gis and app connection as keys for the two entries.
     */
    private static Map<WebSocketSession, Session> sessionsBySocket;

    private static int lastSessionNr = -99;

    static {
        createSessionsMap();
    }

    private static void createSessionsMap(){
        sessionsBySocket = new ConcurrentHashMap<>();
        lastSessionNr = 0;
    }

    static synchronized int getNextSessionNr(){
        lastSessionNr++;
        return lastSessionNr;
    }

    /**
     * Replaces the session collection with a new empty collection
     * and closes all sessions in the old session collection.
     */
    public static int resetSessionCollection() {

        Map<WebSocketSession, Session> oldSessionsMap = null;

        synchronized (Sessions.class){
            oldSessionsMap = sessionsBySocket;
            createSessionsMap();
        }

        List<Session> ses = allSessions(oldSessionsMap).toList();

        for(Session s : ses){
            s.closeConnections(org.springframework.web.socket.CloseStatus.SERVICE_RESTARTED);
        }
        return ses.size();
    }


    /**
     * Finds the session by the instance of the WebSocketSession of one of the SockConnections of the session.
     * returns null if no session can be found for the connection.
     */
    public static Session findByConnection(WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return null;
        }
        return sessionsBySocket.get(webSocketSession);
    }

    /**
     * Finds the session using the given uid of the session.
     * Returns null if the session is not found.
     */
    public static Session findBySessionUid(UUID sessionUid) {
        if (sessionUid == null) {
            return null;
        }

        for (Session session : sessionsBySocket.values()) {
            if (session != null && sessionUid.equals(session.getSessionUid())) {
                return session;
            }
        }

        return null;
    }

    /**
     * Adds a session to the sessions collection and removes
     * any previous occurrences of the session in the collection.
     */
    public static void addOrReplace(Session s){
        if(s == null)
            return;

        WebSocketSession currentApp = s.getAppWebSocket();
        WebSocketSession currentGis = s.getGisWebSocket();

        // remove orphaned entries for this session, but keep entries for the current websockets
        sessionsBySocket.entrySet().removeIf(entry ->
                entry.getValue().getSessionUid().equals(s.getSessionUid())
                && entry.getKey() != currentApp
                && entry.getKey() != currentGis
        );

        if (currentApp != null) {
            sessionsBySocket.put(currentApp, s);
        }
        if (currentGis != null) {
            sessionsBySocket.put(currentGis, s);
        }
    }

    public static void removeSession(Session s){
        sessionsBySocket.values().removeIf(session -> session.equals(s));
    }

    /**
     * Returns all Sessions currently present in the session collection
     */
    public static Stream<Session> allSessions(){
        return allSessions(sessionsBySocket);
    }

    /**
     * Returns all Sessions currently present in the given session mam
     */
    private static Stream<Session> allSessions(Map<WebSocketSession, Session> sessions){
        return sessions.values().stream().distinct();
    }

    /**
     * Grace period for reconnection attempts before removing a session with closed connections.
     * Sessions with closed connections will only be removed after this duration has elapsed,
     * allowing clients time to reconnect (e.g., after network interruptions or app restarts).
     */
    private static final Duration RECONNECTION_GRACE_PERIOD = Duration.ofMinutes(1);

    /**
     * Closes and removes stale sessions from the session collection.
     * A session is stale if:
     * - One or both client connections have been closed for longer than RECONNECTION_GRACE_PERIOD
     * - The maximum delay for finishing the handshake is exceeded
     * Returns a Stream containing the returned session numbers
     */
    public static Stream<Integer> removeStaleSessions() {
        List<Session> staleSessions = Sessions.allSessions()
                .filter(session -> (session.hasStaleClosedConnections(RECONNECTION_GRACE_PERIOD) || session.handShakeExceeded()))
                .sorted().toList();

        for(Session s : staleSessions){
            org.springframework.web.socket.CloseStatus status = s.handShakeExceeded()
                ? org.springframework.web.socket.CloseStatus.POLICY_VIOLATION
                : org.springframework.web.socket.CloseStatus.GOING_AWAY;
            s.closeConnections(status);
            removeSession(s);
        }

        return staleSessions.stream().map(Session::getSessionNr);
    }

    public static List<Session> openSessions(){
        return Sessions.allSessions()
                .filter(session -> !session.hasClosedConnections())
                .filter(session -> session.getAppConnection() != null && session.getGisConnection() != null)
                .toList();
    }

    /**
     * Returns the session containing the connection represented by the key.
     * Returns null, if no matching connection is found.
     */
    public static Session findByConnectionKey(String key, boolean isApp){
        Optional<Session> match = sessionsBySocket.values().stream()
                .filter(ses -> {
                    if(isApp && ses.getAppConnection() != null){
                        return ses.getAppConnection().keyEquals(key);
                    }
                    else if(!isApp && ses.getGisConnection() != null){
                        return ses.getGisConnection().keyEquals(key);
                    }
                    return false;
                }).findFirst();

        return match.orElse(null);
    }
}
