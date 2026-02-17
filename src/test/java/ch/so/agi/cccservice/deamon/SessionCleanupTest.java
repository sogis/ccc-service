package ch.so.agi.cccservice.deamon;

import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.message.MessageAccumulator;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionCleanupTest {

    private SessionsGroomer groomer;

    @BeforeEach
    void resetSessions() throws Exception {
        Sessions.resetSessionCollection();
        groomer = new SessionsGroomer(new MessageAccumulator());
    }

    @Test
    void removeStaleSessionsHandlesEmptyCollection() {
        assertEquals(0, Sessions.allSessions().count());

        groomer.removeStaleSessions();

        assertEquals(0, Sessions.allSessions().count());
    }

    @Test
    void removeStaleSessionsRemovesAllStaleEntries() throws IOException {
        TestUtil.initSession().getAppWebSocket().close();
        TestUtil.initSession().getGisWebSocket().close();

        assertEquals(2, Sessions.allSessions().count());

        groomer.removeStaleSessions();

        assertEquals(0, Sessions.allSessions().count());
    }

    @Test
    void removeStaleSessionsOnlyRemovesStaleEntries() throws IOException {
        TestUtil.initSession();
        TestUtil.initSession().getGisWebSocket().close();

        assertEquals(2, Sessions.allSessions().count());

        groomer.removeStaleSessions();

        assertEquals(1, Sessions.allSessions().count());
    }

    @Test
    void peerConnectionClosedAfterCleanup() throws IOException {
        Session s = TestUtil.initSession();
        s.getAppWebSocket().close();

        groomer.removeStaleSessions();

        assertFalse(s.getGisWebSocket().isOpen());
    }

    @Test
    void connectionClosedAfterHandshakeExpiryAndCleanup(){
        Session s = TestUtil.openSession(true);
        s.setHandShakeMaxDuration(Duration.ofMillis(10));

        TestUtil.wait(20);

        groomer.removeStaleSessions();

        assertFalse(s.getAppWebSocket().isOpen());
    }

    // --- V1.0: keine Grace Period, sofort stale ---

    @Test
    void v10_sessionRemovedImmediatelyWhenConnectionClosed() throws IOException {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);
        Sessions.addOrReplace(s);
        s.getAppWebSocket().close();

        groomer.removeStaleSessions();

        assertEquals(0, Sessions.allSessions().count());
    }

    // --- V1.2: Grace Period, nicht sofort stale ---

    @Test
    void v12_sessionNotRemovedWithinGracePeriod() throws IOException {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        Sessions.addOrReplace(s);

        s.getAppWebSocket().close();
        s.markConnectionClosed(s.getAppWebSocket());

        groomer.removeStaleSessions();

        assertEquals(1, Sessions.allSessions().count(), "V1.2 session should survive groomer within grace period");
    }

    @Test
    void v12_sessionNotRemovedWhenBothConnectionsOpen() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        Sessions.addOrReplace(s);

        groomer.removeStaleSessions();

        assertEquals(1, Sessions.allSessions().count());
    }

    @Test
    void v12_mixedWithV10_onlyV10RemovedImmediately() throws IOException {
        Session v10 = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);
        Sessions.addOrReplace(v10);
        Session v12 = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        Sessions.addOrReplace(v12);

        v10.getAppWebSocket().close();
        v12.getAppWebSocket().close();
        v12.markConnectionClosed(v12.getAppWebSocket());

        assertEquals(2, Sessions.allSessions().count());

        groomer.removeStaleSessions();

        assertEquals(1, Sessions.allSessions().count(), "Only V1.0 session should be removed, V1.2 still within grace period");
    }

}
