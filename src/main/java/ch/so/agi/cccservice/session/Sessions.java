package ch.so.agi.cccservice.session;

import org.springframework.web.socket.WebSocketSession;

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

    static {
        createSessionsMap();
    }

    private static void createSessionsMap(){
        sessionsBySocket = new ConcurrentHashMap<>();
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
            s.closeConnections();
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

        // remove by the old sockets of the session
        Session old = findBySessionUid(s.getSessionUid());
        if(old != null){
            sessionsBySocket.remove(old.getAppWebSocket());
            sessionsBySocket.remove(old.getGisWebSocket());
        }

        if (s.getAppWebSocket() != null) {
            sessionsBySocket.put(s.getAppWebSocket(), s);
        }
        if (s.getGisWebSocket() != null) {
            sessionsBySocket.put(s.getGisWebSocket(), s);
        }
    }

    private static void removeSession(Session s){
        if(s.getGisWebSocket() != null)
            sessionsBySocket.remove(s.getGisWebSocket());

        if(s.getAppWebSocket() != null)
            sessionsBySocket.remove(s.getAppWebSocket());
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
     * Closes and removes stale sessions from the session collection.
     * A session is stale if:
     * - One or both client connections are closed
     * - The maximum delay for finishing the handshake is exceeded
     * Returns a Stream containing the returned session numbers
     */
    public static Stream<Integer> removeStaleSessions() {
        List<Session> staleSessions = Sessions.allSessions()
                .filter(session -> (session.hasClosedConnections() || session.handShakeExceeded()))
                .sorted().toList();

        for(Session s : staleSessions){
            s.closeConnections();
            removeSession(s);
        }

        return staleSessions.stream().map(Session::getSessionNr);
    }

    public static List<Session> openSessions(){
        return Sessions.allSessions()
                .filter(session -> !session.hasClosedConnections())
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
