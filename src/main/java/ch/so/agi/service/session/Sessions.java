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
     * Adds a session to the sessions collection.
     */
    public static synchronized void add(Session s){
        sessionsBySocket.remove(s.getAppWebSocket());
        sessionsBySocket.remove(s.getGisWebSocket());
        sessionsBySocket.put(s.getAppWebSocket(), s);
        sessionsBySocket.put(s.getGisWebSocket(), s);
    }
}
