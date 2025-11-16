package ch.so.agi.service.session;

import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

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
}
