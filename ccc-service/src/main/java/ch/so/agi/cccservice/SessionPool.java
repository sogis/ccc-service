package ch.so.agi.cccservice;

import org.springframework.web.socket.WebSocketSession;

public class SessionPool {
    public void addSession(SessionId sessionId, SessionState sessionState) {

    }
    public SessionState getSession(SessionId sessionId) {
        return null;
    }
    public void addAppWebSocketSession(SessionId sessionId, WebSocketSession webSocketSession) {

    }
    public void addGisWebSocketSession(SessionId sessionId, WebSocketSession webSocketSession) {

    }
    public WebSocketSession getAppWebSocketSession(SessionId sessionId) {
        return null;
    }
    public WebSocketSession getGisWebSocketSession(SessionId sessionId) {
        return null;
    }
    public SessionId getSessionId(WebSocketSession webSocketSession) {
        return null;
    }
    public void removeSession(SessionId sessionId) {

    }
}
