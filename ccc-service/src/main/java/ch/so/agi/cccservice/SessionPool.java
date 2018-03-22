package ch.so.agi.cccservice;

import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;

public class SessionPool {
    private HashMap<SessionId, SessionState> sessionStates = new HashMap<>();
    private HashMap<SessionId, WebSocketSession> idToAppSocket = new HashMap<>();
    private HashMap<SessionId, WebSocketSession> idToGisSocket = new HashMap<>();
    private HashMap<WebSocketSession, SessionId> socketToId = new HashMap<>();


    public void addSession(SessionId sessionId, SessionState sessionState) throws IllegalArgumentException {
        if (sessionStates.get(sessionId) == null) {
            sessionStates.put(sessionId, sessionState);
        } else {
            throw new IllegalArgumentException("SessionID already exists: " + sessionId.getSessionId());
        }
    }

    public SessionState getSession(SessionId sessionId) {
        return sessionStates.get(sessionId);
    }

    public void addAppWebSocketSession(SessionId sessionId, WebSocketSession webSocketSession) {

        if (idToAppSocket.get(sessionId) == null) {
            idToAppSocket.put(sessionId, webSocketSession);
        } else {
            throw new IllegalArgumentException("App-WebSocketSession for sessionID " + sessionId.getSessionId() +
                    " already exists.");
        }

        if (socketToId.get(webSocketSession) == null){
            socketToId.put(webSocketSession, sessionId);
        } else {
            throw new IllegalArgumentException("App-WebSocketSession for sessionID " + sessionId.getSessionId() +
                    " already exists.");
        }
    }

    public void addGisWebSocketSession(SessionId sessionId, WebSocketSession webSocketSession) {
        if (idToGisSocket.get(sessionId) == null) {
            idToGisSocket.put(sessionId, webSocketSession);
        } else {
            throw new IllegalArgumentException("GIS-WebSocketSession for sessionID " + sessionId.getSessionId() +
                    " already exists.");
        }

        if (socketToId.get(webSocketSession) == null){
            socketToId.put(webSocketSession, sessionId);
        } else {
            throw new IllegalArgumentException("GIS-WebSocketSession for sessionID " + sessionId.getSessionId() +
                    " already exists.");
        }
    }

    public WebSocketSession getAppWebSocketSession(SessionId sessionId) {
        return idToAppSocket.get(sessionId);
    }

    public WebSocketSession getGisWebSocketSession(SessionId sessionId) {
        return idToGisSocket.get(sessionId);
    }

    public SessionId getSessionId(WebSocketSession webSocketSession) {
        return socketToId.get(webSocketSession);
    }

    public void removeSession(SessionId sessionId) {
        WebSocketSession gisSocket = idToGisSocket.get(sessionId);
        WebSocketSession appSocket = idToAppSocket.get(sessionId);
        SessionState sessionState = sessionStates.get(sessionId);
        SessionId gisSessionId = socketToId.get(gisSocket);
        SessionId appSessionId = socketToId.get(appSocket);

        if (gisSessionId != null){
            socketToId.remove(gisSocket);
        } //Fehlermeldung falls nicht existiert?

        if (appSessionId != null){
            socketToId.remove(appSocket);
        }//Fehlermeldung falls nicht existiert?

        if (sessionState != null) {
            sessionStates.remove(sessionId);
        } else {
            throw new IllegalArgumentException("Session does not happen to have a sessionState");
        }

        if (gisSocket != null){
            idToGisSocket.remove(sessionId);
        } //Fehlermeldung falls nicht existiert?

        if (appSocket != null){
            idToAppSocket.remove(sessionId);
        }//Fehlermeldung falls nicht existiert?
    }
}