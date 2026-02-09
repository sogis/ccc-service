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

    // --- openSessions ---

    @Test
    void openSessions_excludesPartiallySetUpSessions() {
        // Session mit nur einer Connection (App, keine GIS)
        SockConnection appCon = new SockConnection("app-client", "1.0", new MockWebSocketSession());
        Session partial = new Session(UUID.randomUUID(), appCon, true);
        Sessions.addOrReplace(partial);

        // Vollständig eingerichtete Session
        Session complete = createSession(new MockWebSocketSession(), new MockWebSocketSession());
        Sessions.addOrReplace(complete);

        List<Session> openSessions = Sessions.openSessions();

        assertEquals(1, openSessions.size());
        assertSame(complete, openSessions.get(0));
    }

    @Test
    void openSessions_excludesSessionsWithClosedConnections() throws IOException {
        // Vollständige Session mit geschlossener Connection
        MockWebSocketSession closedApp = new MockWebSocketSession();
        Session closedSession = createSession(closedApp, new MockWebSocketSession());
        Sessions.addOrReplace(closedSession);
        closedApp.close();

        // Offene Session
        Session openSession = createSession(new MockWebSocketSession(), new MockWebSocketSession());
        Sessions.addOrReplace(openSession);

        List<Session> openSessions = Sessions.openSessions();

        assertEquals(1, openSessions.size());
        assertSame(openSession, openSessions.get(0));
    }

    @Test
    void openSessions_returnsOnlyFullySetUpAndOpenSessions() throws IOException {
        // Vollständig eingerichtete offene Session
        Session open = createSession(new MockWebSocketSession(), new MockWebSocketSession());
        Sessions.addOrReplace(open);

        // Teilweise eingerichtete Session
        SockConnection partialCon = new SockConnection("app", "1.0", new MockWebSocketSession());
        Session partial = new Session(UUID.randomUUID(), partialCon, true);
        Sessions.addOrReplace(partial);

        // Vollständige aber geschlossene Session
        MockWebSocketSession closedSocket = new MockWebSocketSession();
        Session closed = createSession(closedSocket, new MockWebSocketSession());
        Sessions.addOrReplace(closed);
        closedSocket.close();

        List<Session> openSessions = Sessions.openSessions();

        assertEquals(1, openSessions.size());
        assertSame(open, openSessions.get(0));
    }
}
