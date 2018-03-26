package ch.so.agi.cccservice;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import java.util.HashMap;
import java.util.Map;


public class SessionPoolTest {
    private SessionId sessionId = new SessionId("{123-456-789-0}");
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

        Assert.assertEquals(sessionState, savedSessionState);
    }
/* ToDo: Entfernen, da in addSession integriert?
    @Test
    public void getSession() {
    }*/

    @Test
    public void addAppWebSocketSession() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addAppWebSocketSession(sessionId, appWebSocketSession);

        WebSocketSession savedWebSocketSession = sessionPool.getAppWebSocketSession(sessionId);

        Assert.assertEquals(appWebSocketSession,savedWebSocketSession );
    }

    @Test
    public void addGisWebSocketSession() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addGisWebSocketSession(sessionId, gisWebSocketSession);

        WebSocketSession savedWebSocketSession = sessionPool.getGisWebSocketSession(sessionId);

        Assert.assertEquals(gisWebSocketSession, savedWebSocketSession);
    }

    /* ToDo: Entfernen da in add.... integriert?
    @Test
    public void getAppWebSocketSession() {
    }

    @Test
    public void getGisWebSocketSession() {
    }*/

    @Test
    public void getSessionId() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addGisWebSocketSession(sessionId, gisWebSocketSession);

        SessionId savedSessionId = sessionPool.getSessionId(gisWebSocketSession);

        Assert.assertEquals(sessionId, savedSessionId);
    }

    @Test
    public void removeSessionFromSessionStatesHashMap() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addSession(sessionId, sessionState);

        sessionPool.removeSession(sessionId);

        Assert.assertNull(sessionPool.getSession(sessionId));
    }

    @Test
    public void removeSessionFromIdToAppSocketHashMap() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addSession(sessionId, sessionState);
        sessionPool.addAppWebSocketSession(sessionId, appWebSocketSession);

        sessionPool.removeSession(sessionId);

        Assert.assertNull(sessionPool.getAppWebSocketSession(sessionId));
        Assert.assertNull(sessionPool.getSessionId(appWebSocketSession));
    }

    @Test
    public void removeSessionFromIdToGisSocketHashMap() {
        SessionPool sessionPool = new SessionPool();
        sessionPool.addSession(sessionId, sessionState);
        sessionPool.addGisWebSocketSession(sessionId, gisWebSocketSession);

        sessionPool.removeSession(sessionId);

        Assert.assertNull(sessionPool.getGisWebSocketSession(sessionId));
        Assert.assertNull(sessionPool.getSessionId(gisWebSocketSession));
    }

    @Test (expected=IllegalArgumentException.class)
    public void removeInexistentSessionFromSessionStateHashMap(){
        SessionPool sessionPool = new SessionPool();
        sessionPool.removeSession(sessionId);
    }
}