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

        Sessions.add(session);

        assertSame(session, Sessions.findByConnection(appSession));
        assertSame(session, Sessions.findByConnection(gisSession));
    }

    @Test
    void addReplacesExistingSessionForSameSocket() {
        MockWebSocketSession sharedSession = new MockWebSocketSession();
        MockWebSocketSession gisSession = new MockWebSocketSession();
        Session original = createSession(sharedSession, gisSession);
        Sessions.add(original);

        MockWebSocketSession newGisSession = new MockWebSocketSession();
        Session replacement = createSession(sharedSession, newGisSession);
        Sessions.add(replacement);

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
        SockConnection appConnection = new SockConnection("app-client", "1.0", appSession);
        Session session = new Session(UUID.randomUUID(), appConnection, true);

        SockConnection gisConnection = new SockConnection("gis-client", "1.0", gisSession);
        session.tryToAddSecondConnection(gisConnection, false);
        return session;
    }
}
