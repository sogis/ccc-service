package ch.so.agi.cccservice;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;

/**
 * Collection of active ccc sessions (Id, WebSocketSession, SessionStates)
 */
@Component
public class SessionPool {
    private HashMap<SessionId, SessionState> sessionStates = new HashMap<>();
    private HashMap<SessionId, WebSocketSession> idToAppSocket = new HashMap<>();
    private HashMap<SessionId, WebSocketSession> idToGisSocket = new HashMap<>();
    private HashMap<WebSocketSession, SessionId> socketToId = new HashMap<>();


    /**
     * Adds a SessionId with its SessionState to the sessionStates-Hashmap
     * @param sessionId of connection
     * @param sessionState of sessionId
     * @throws IllegalArgumentException thrown on already existing sessionId in sessionStates-HashMap
     */
    public void addSession(SessionId sessionId, SessionState sessionState) throws IllegalArgumentException {
        if (sessionStates.get(sessionId) == null) {
            sessionStates.put(sessionId, sessionState);
        } else {
            throw new IllegalArgumentException("SessionID already exists: " + sessionId.getSessionId());
        }
    }

    /**
     * Returns SessionState of specific Session
     * @param sessionId specific Session
     * @return SessionState of specific Session
     */
    public SessionState getSession(SessionId sessionId) {
        return sessionStates.get(sessionId);
    }

    /**
     * Adds WebSocketSession of the application to the SessionId in the idToAppSocket-Hashmap and the socketToId-Hashmap
     * @param sessionId of specific connection
     * @param webSocketSession of application
     */
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

    /**
     * Adds WebSocketSession of the GIS to the SessionId in the idToGisSocket-Hashmap and the socketToId-Hashmap
     * @param sessionId of specific connection
     * @param webSocketSession of GIS
     */
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

    /**
     * Gets WebSocketSession of Application of a specific SessionId
     * @param sessionId specific Connection
     * @return WebsocketSession of Application
     */
    public WebSocketSession getAppWebSocketSession(SessionId sessionId) {
        return idToAppSocket.get(sessionId);
    }

    /**
     * Gets WebSocketSession of GIS of a specific SessionId
     * @param sessionId specific Connection
     * @return WebSocketSession of GIS
     */
    public WebSocketSession getGisWebSocketSession(SessionId sessionId) {
        return idToGisSocket.get(sessionId);
    }

    /**
     * Gets SessionId of either a WebSocketSession of an application or of a WebSocketSession of GIS
     * @param webSocketSession either of application or GIS
     * @return SessionID of Connection
     */
    public SessionId getSessionId(WebSocketSession webSocketSession) {
        return socketToId.get(webSocketSession);
    }

    /**
     * Removes closed Session in the Sessionpool (in all Hashmaps)
     * @param sessionId to remove
     */
    public void removeSession(SessionId sessionId) {
        WebSocketSession gisSocket = idToGisSocket.get(sessionId);
        WebSocketSession appSocket = idToAppSocket.get(sessionId);
        SessionState sessionState = sessionStates.get(sessionId);
        SessionId gisSessionId = socketToId.get(gisSocket);
        SessionId appSessionId = socketToId.get(appSocket);

        if (gisSessionId != null){
            socketToId.remove(gisSocket);
        }

        if (appSessionId != null){
            socketToId.remove(appSocket);
        }

        if (sessionState != null) {
            sessionStates.remove(sessionId);
        } else {
            throw new IllegalArgumentException("Session does not happen to have a sessionState");
        }

        if (gisSocket != null){
            idToGisSocket.remove(sessionId);
        }

        if (appSocket != null){
            idToAppSocket.remove(sessionId);
        }
    }

    /** gets the type of the attached client.
     * @param socket web socket to client. 
     * @return APP or GIS
     */
    public int getClientType(WebSocketSession socket) {
        SessionId id=socketToId.get(socket);
        if(idToAppSocket.get(id)==socket) {
            return Service.APP;
        }else if(idToGisSocket.get(id)==socket) {
            return Service.GIS;
        }
        throw new IllegalStateException();
    }
}