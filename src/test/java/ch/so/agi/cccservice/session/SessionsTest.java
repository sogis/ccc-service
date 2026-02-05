package ch.so.agi.cccservice.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionsTest {

    @BeforeEach
    void resetSessions() throws Exception {
        Sessions.resetSessionCollection();
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

    // --- findBySessionUid ---

    @Test
    void findBySessionUid_returnsMatchingSession() {
        UUID uid = UUID.randomUUID();
        Session session = createSession(uid, new MockWebSocketSession(), new MockWebSocketSession());
        Sessions.addOrReplace(session);

        assertSame(session, Sessions.findBySessionUid(uid));
    }

    @Test
    void findBySessionUid_returnsNullForUnknownUid() {
        Session session = createSession(new MockWebSocketSession(), new MockWebSocketSession());
        Sessions.addOrReplace(session);

        assertNull(Sessions.findBySessionUid(UUID.randomUUID()));
        assertNull(Sessions.findBySessionUid(null));
    }

    // --- removeStaleSessions ---

    @Test
    void removeStaleSessions_removesClosedConnection() throws IOException {
        Session healthy = createSession(new MockWebSocketSession(), new MockWebSocketSession());
        Sessions.addOrReplace(healthy);

        MockWebSocketSession staleApp = new MockWebSocketSession();
        Session stale = createSession(staleApp, new MockWebSocketSession());
        Sessions.addOrReplace(stale);

        staleApp.close();

        List<Integer> removed = Sessions.removeStaleSessions().toList();

        assertEquals(1, removed.size());
        assertEquals(stale.getSessionNr(), removed.get(0));
        assertEquals(1, Sessions.allSessions().count());
    }

    @Test
    void removeStaleSessions_removesHandshakeExceeded() {
        Session healthy = createSession(new MockWebSocketSession(), new MockWebSocketSession());
        Sessions.addOrReplace(healthy);

        // Incomplete session (only app connection, no gis) with expired handshake window
        SockConnection appCon = new SockConnection("app-client", "1.0", new MockWebSocketSession());
        Session expired = new Session(UUID.randomUUID(), appCon, true);
        expired.setHandShakeMaxDuration(Duration.ZERO);
        Sessions.addOrReplace(expired);

        List<Integer> removed = Sessions.removeStaleSessions().toList();

        assertEquals(1, removed.size());
        assertEquals(expired.getSessionNr(), removed.get(0));
        assertEquals(1, Sessions.allSessions().count());
    }

    @Test
    void removeStaleSessions_keepsHealthySessions() {
        Session s1 = createSession(new MockWebSocketSession(), new MockWebSocketSession());
        Session s2 = createSession(new MockWebSocketSession(), new MockWebSocketSession());
        Sessions.addOrReplace(s1);
        Sessions.addOrReplace(s2);

        List<Integer> removed = Sessions.removeStaleSessions().toList();

        assertEquals(0, removed.size());
        assertEquals(2, Sessions.allSessions().count());
    }

    // --- findByConnectionKey ---

    @Test
    void findByConnectionKey_findsAppConnection() {
        Session session = createSession(new MockWebSocketSession(), new MockWebSocketSession());
        Sessions.addOrReplace(session);

        String appKey = session.getAppConnection().getConnectionKey();
        assertSame(session, Sessions.findByConnectionKey(appKey, true));
    }

    @Test
    void findByConnectionKey_findsGisConnection() {
        Session session = createSession(new MockWebSocketSession(), new MockWebSocketSession());
        Sessions.addOrReplace(session);

        String gisKey = session.getGisConnection().getConnectionKey();
        assertSame(session, Sessions.findByConnectionKey(gisKey, false));
    }

    @Test
    void findByConnectionKey_returnsNullForUnknownKey() {
        Session session = createSession(new MockWebSocketSession(), new MockWebSocketSession());
        Sessions.addOrReplace(session);

        assertNull(Sessions.findByConnectionKey("unknown", true));
        assertNull(Sessions.findByConnectionKey("unknown", false));
    }
}
