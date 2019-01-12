package ch.so.agi.cccservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Collection of active ccc-sessions. One ccc-session has an id, one or two associated 
 * web sockets and a session state.
 */
@Component
public class SessionPool {
    private HashMap<SessionId, SessionState> sessionStates = new HashMap<>();
    private HashMap<SessionId, WebSocketSession> idToAppSocket = new HashMap<>();
    private HashMap<SessionId, WebSocketSession> idToGisSocket = new HashMap<>();
    private HashMap<WebSocketSession, SessionId> socketToId = new HashMap<>();
    private HashMap<SessionId, Long> sessionActivity = new HashMap<>();
    Logger logger = LoggerFactory.getLogger(SessionPool.class);

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
        logger.debug("removeSession "+sessionId.getSessionId());
        WebSocketSession gisSocket = idToGisSocket.get(sessionId);
        if(gisSocket!=null) {
            idToGisSocket.remove(sessionId);
            SessionId gisSessionId = socketToId.get(gisSocket);
            if (gisSessionId != null){
                socketToId.remove(gisSocket);
            }
        }

        WebSocketSession appSocket = idToAppSocket.get(sessionId);
        if(appSocket!=null) {
            idToAppSocket.remove(sessionId);
            SessionId appSessionId = socketToId.get(appSocket);
            if (appSessionId != null){
                socketToId.remove(appSocket);
            }
        }

        SessionState sessionState = sessionStates.get(sessionId);
        if (sessionState != null) {
            sessionStates.remove(sessionId);
        }
        if (gisSocket!=null && isSocketOpen(gisSocket)) {
            try {
                gisSocket.close();
            } catch (IOException e) {
                throw new IllegalArgumentException("Can not Close gisSocket");
            }
        }

        if (appSocket!=null && isSocketOpen(appSocket)) {
            try {
                appSocket.close();
            } catch (IOException e) {
                throw new IllegalArgumentException("Can not Close appSocket");
            }
        }
        sessionActivity.remove(sessionId);
    }

    public static boolean isSocketOpen(WebSocketSession gisSocket) {
        return gisSocket.getRemoteAddress()!=null && gisSocket.isOpen();
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
    /** get the name of the client associated with the socket.
     * Can only be called after the connect message has been processed.
     * @param socket web socket to client. 
     * @return client name as received by the connect message
     */
    public String getClientName(WebSocketSession socket) {
        SessionId id=socketToId.get(socket);
        if(idToAppSocket.get(id)==socket) {
            return getSession(id).getAppName();
        }else if(idToGisSocket.get(id)==socket) {
            return getSession(id).getGisName();
        }
        throw new IllegalStateException();
    }
    public void checkActivityTimeout(SessionId sessionId,long maxInactivity) throws ServiceException {
        Long lastActivity=sessionActivity.get(sessionId);
        long current=System.currentTimeMillis();
        if(lastActivity!=null && current-lastActivity>maxInactivity*1000) {
            throw new ServiceException(506, "Session-Timeout");
        }
        sessionActivity.put(sessionId, current);
    }
    public void closeInactiveSessions(long maxInactivity) {
        logger.debug("closeInactiveSessions maxInactivity "+maxInactivity);
        long current=System.currentTimeMillis();
        java.util.Iterator<Entry<SessionId, Long>> it=sessionActivity.entrySet().iterator();
        while(it.hasNext()) {
            Entry<SessionId, Long> entry=it.next();
            if(current-entry.getValue()>maxInactivity*1000) {
                it.remove(); // remove it here to avoid concurrent modification exception in removeSession()
                removeSession(entry.getKey());
                logger.info("Session "+entry.getKey()+" closed due to inactivity");
            }
        }
    }

}