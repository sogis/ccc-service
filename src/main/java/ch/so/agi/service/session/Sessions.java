package ch.so.agi.service.session;

import ch.so.agi.cccprobe.ProbeTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sessions {
    private static final Map<WebSocketSession, Session> sessionsBySocket = new HashMap<>();


    /**
     * Finds the session by the instance of the WebSocketSession of one of the SockConnections of the session.
     * returns null if no session can be found for the connection.
     */
    public static synchronized Session findByConnection(WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return null;
        }
        return sessionsBySocket.get(webSocketSession);
    }

    /**
     * Finds the session using the given uid of the session.
     * Returns null if the session is not found.
     */
    public static synchronized Session findBySessionUid(UUID sessionUid) {
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
    public static synchronized void addOrReplace(Session s){
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

    private static synchronized void removeSession(Session s){
        sessionsBySocket.remove(s.getGisWebSocket());
        sessionsBySocket.remove(s.getAppWebSocket());
    }

    /**
     * Returns all Sessions currently present in the session collection
     */
    public static synchronized Stream<Session> allSessions(){
        return sessionsBySocket.values().stream().distinct();
    }

    /**
     * Closes and removes stale sessions from the session collection.
     * A session is stale if:
     * - One or both client connections are closed
     * - The maximum delay for finishing the handshake is exceeded
     * Returns a Stream containing the returned session numbers
     */
    public static synchronized Stream<Integer> removeStaleSessions() {
        List<Session> staleSessions = Sessions.allSessions()
                .filter(session -> (session.hasClosedConnections() || session.handShakeExceeded()))
                .sorted().toList();

        for(Session s : staleSessions){
            s.closeConnections();
            removeSession(s);
        }

        return staleSessions.stream().map(Session::getSessionNr);
    }

    /**
     * For the unit tests
     */
    public static void removeAll() {
        sessionsBySocket.clear();
    }
}
