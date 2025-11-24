package ch.so.agi.cccservice.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionsTest {

    @BeforeEach
    void resetSessions() throws Exception {
        Sessions.removeAll();
    }

    private Session createSession(MockWebSocketSession appSession, MockWebSocketSession gisSession) {
        return  createSession(UUID.randomUUID(), appSession, gisSession);
    }

    private Session createSession(UUID sessionUid, MockWebSocketSession appSession, MockWebSocketSession gisSession) {
        SockConnection appConnection = new SockConnection("app-client", "1.0", appSession);
        Session session = new Session(sessionUid, appConnection, true);

        SockConnection gisConnection = new SockConnection("gis-client", "1.0", gisSession);
        assertTrue(session.tryToAddSecondConnection(gisConnection, false));
        return session;
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
}
