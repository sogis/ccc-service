package ch.so.agi.service.session;

import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.HashMap;

public class Sessions {
    private static final Map<WebSocketSession, Session> sessionsBySocket = new HashMap<>();

    public static synchronized Session findByConnection(WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return null;
        }
        return sessionsBySocket.get(webSocketSession);
    }

    public static synchronized void add(Session s){
        sessionsBySocket.remove(s.getAppWebSocket());
        sessionsBySocket.remove(s.getGisWebSocket());
        sessionsBySocket.put(s.getAppWebSocket(), s);
        sessionsBySocket.put(s.getGisWebSocket(), s);
    }
}
