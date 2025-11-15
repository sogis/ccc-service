package ch.so.agi.service.session;

import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class Sessions {
    private static final Map<WebSocketSession, Session> sessionsBySocket = new HashMap<>();

    public static synchronized Session findByConnection(WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return null;
        }
        return sessionsBySocket.get(webSocketSession);
    }

    public static synchronized Session findBySession(UUID sessionUid) {
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

    public static synchronized void add(Session s){
        sessionsBySocket.remove(s.getAppWebSocket());
        sessionsBySocket.remove(s.getGisWebSocket());
        sessionsBySocket.put(s.getAppWebSocket(), s);
        sessionsBySocket.put(s.getGisWebSocket(), s);
    }

    public static synchronized Session findByUid(UUID sessionUid){
        return null;
    }
}
