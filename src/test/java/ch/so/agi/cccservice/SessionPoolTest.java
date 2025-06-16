package ch.so.agi.cccservice;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class SessionPoolTest {
    private SessionId sessionId = new SessionId("{235ea7d3-8069-4bbc-b7de-17ff15239e7c}");
    private SessionState sessionState = new SessionState();
    private final HttpHeaders headers = new HttpHeaders();
    private final Map<String, Object> attributes = new HashMap<>();
    private WebSocketSession appWebSocketSession =
            new StandardWebSocketSession(this.headers, this.attributes, null, null);
    private WebSocketSession gisWebSocketSession =
            new StandardWebSocketSession(this.headers, this.attributes, null, null);



    @Test
    public void addSession() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addSession(sessionId, sessionState);

        SessionState savedSessionState = sessionPool.getSession(sessionId);

        assertEquals(sessionState, savedSessionState);
    }
    @Test
    public void closeInactiveSessions() throws Exception {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addSession(sessionId, sessionState);

        SessionState savedSessionState = sessionPool.getSession(sessionId);
        sessionPool.checkActivityTimeout(sessionId, Service.DEFAULT_MAX_INACTIVITYTIME);
        TimeUnit.SECONDS.sleep(2);
        sessionPool.closeInactiveSessions(1);
        assertNull(sessionPool.getSession(sessionId));
    }

    @Test
    public void addAppWebSocketSession() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addAppWebSocketSession(sessionId, appWebSocketSession);

        WebSocketSession savedWebSocketSession = sessionPool.getAppWebSocketSession(sessionId);

        assertEquals(appWebSocketSession,savedWebSocketSession );
    }

    @Test
    public void addGisWebSocketSession() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addGisWebSocketSession(sessionId, gisWebSocketSession);

        WebSocketSession savedWebSocketSession = sessionPool.getGisWebSocketSession(sessionId);

        assertEquals(gisWebSocketSession, savedWebSocketSession);
    }


    @Test
    public void getSessionId() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addGisWebSocketSession(sessionId, gisWebSocketSession);

        SessionId savedSessionId = sessionPool.getSessionId(gisWebSocketSession);

        assertEquals(sessionId, savedSessionId);
    }

    @Test
    public void removeSessionFromSessionStatesHashMap() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addSession(sessionId, sessionState);

        sessionPool.removeSession(sessionId);

        assertNull(sessionPool.getSession(sessionId));
    }

    @Test
    public void removeSessionFromIdToAppSocketHashMap() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addSession(sessionId, sessionState);
        sessionPool.addAppWebSocketSession(sessionId, appWebSocketSession);

        sessionPool.removeSession(sessionId);

        assertNull(sessionPool.getAppWebSocketSession(sessionId));
        assertNull(sessionPool.getSessionId(appWebSocketSession));
    }

    @Test
    public void removeSessionFromIdToGisSocketHashMap() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addSession(sessionId, sessionState);
        sessionPool.addGisWebSocketSession(sessionId, gisWebSocketSession);

        sessionPool.removeSession(sessionId);

        assertNull(sessionPool.getGisWebSocketSession(sessionId));
        assertNull(sessionPool.getSessionId(gisWebSocketSession));
    }

}
