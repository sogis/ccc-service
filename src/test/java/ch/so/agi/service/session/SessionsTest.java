package ch.so.agi.service.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionsTest {

    @BeforeEach
    void resetSessions() throws Exception {
        Field field = Sessions.class.getDeclaredField("sessionsBySocket");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<WebSocketSession, Session> map = (Map<WebSocketSession, Session>) field.get(null);
        map.clear();
    }

    @Test
    void findByConnectionReturnsSessionForRegisteredSocket() {
        MockWebSocketSession appSession = new MockWebSocketSession();
        MockWebSocketSession gisSession = new MockWebSocketSession();
        Session session = createSession(appSession, gisSession);

        Sessions.addOrReplace(session);

        assertSame(session, Sessions.findByConnection(appSession));
        assertSame(session, Sessions.findByConnection(gisSession));
    }

    @Test
    void addReplacesExistingSessionForSameSocket() {
        UUID sesUid = UUID.randomUUID();
        MockWebSocketSession sharedSession = new MockWebSocketSession();
        MockWebSocketSession gisSession = new MockWebSocketSession();
        Session original = createSession(sesUid, sharedSession, gisSession);
        Sessions.addOrReplace(original);

        MockWebSocketSession newGisSession = new MockWebSocketSession();
        Session replacement = createSession(sesUid, sharedSession, newGisSession);
        Sessions.addOrReplace(replacement);

        assertSame(replacement, Sessions.findByConnection(sharedSession));
        assertSame(replacement, Sessions.findByConnection(newGisSession));
        assertNull(Sessions.findByConnection(gisSession));
    }

    @Test
    void findByConnectionReturnsNullWhenSessionUnknown() {
        MockWebSocketSession unknownSession = new MockWebSocketSession();

        assertNull(Sessions.findByConnection(unknownSession));
        assertNull(Sessions.findByConnection(null));
    }

    private Session createSession(MockWebSocketSession appSession, MockWebSocketSession gisSession) {
        return  createSession(UUID.randomUUID(), appSession, gisSession);
    }

    private Session createSession(UUID sessionUid, MockWebSocketSession appSession, MockWebSocketSession gisSession) {
        SockConnection appConnection = new SockConnection("app-client", "1.0", appSession);
        Session session = new Session(sessionUid, appConnection, true, Session.DEFAULT_HANDSHAKE_MAX_DURATION);

        SockConnection gisConnection = new SockConnection("gis-client", "1.0", gisSession);
        assertTrue(session.tryToAddSecondConnection(gisConnection, false));
        return session;
    }
}
