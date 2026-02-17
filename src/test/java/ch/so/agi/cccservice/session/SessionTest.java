package ch.so.agi.cccservice.session;

import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.exception.HandshakeIncompleteException;
import ch.so.agi.cccservice.exception.ReceivingConnectionClosedException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @Test
    void secondConnectionAcceptedWithinHandshakeWindow() {
        UUID sessionUid = UUID.randomUUID();
        MockWebSocketSession gisWebSocket = new MockWebSocketSession();
        SockConnection gisConnection = new SockConnection("gis-client", "1.0", gisWebSocket);
        Session session = new Session(sessionUid, gisConnection, false);

        MockWebSocketSession appWebSocket = new MockWebSocketSession();
        SockConnection appConnection = new SockConnection("app-client", "1.0", appWebSocket);

        boolean added = session.tryToAddSecondConnection(appConnection, true);

        assertTrue(added);
        assertSame(appWebSocket, session.getAppWebSocket());
        assertSame(gisWebSocket, session.getGisWebSocket());
    }

    @Test
    void secondConnectionRejectedAfterHandshakeWindowElapsed() throws Exception {
        UUID sessionUid = UUID.randomUUID();
        MockWebSocketSession gisWebSocket = new MockWebSocketSession();
        SockConnection gisConnection = new SockConnection("gis-client", "1.0", gisWebSocket);
        Session session = new Session(sessionUid, gisConnection, false);
        session.setHandShakeMaxDuration(Duration.ofMillis(100));

        TestUtil.wait(110);

        MockWebSocketSession appWebSocket = new MockWebSocketSession();
        SockConnection appConnection = new SockConnection("app-client", "1.0", appWebSocket);

        boolean added = session.tryToAddSecondConnection(appConnection, true);

        assertFalse(added);
        assertNull(session.getAppWebSocket());
        assertSame(gisWebSocket, session.getGisWebSocket());
    }

    @Test
    void assertConnected_passes() {
        Session s = TestUtil.initSession();
        s.assertConnected();
    }

    @Test
    void assertConnected_fails_on_unfinished_handshake() {
        UUID sessionUid = UUID.randomUUID();
        MockWebSocketSession gisWebSocket = new MockWebSocketSession();
        SockConnection gisConnection = new SockConnection("gis-client", "1.0", gisWebSocket);
        Session session = new Session(sessionUid, gisConnection, false);

        assertThrows(HandshakeIncompleteException.class, session::assertConnected);
    }

    @Test
    void assertConnected_fails_on_closed_socket() {
        Session s = TestUtil.initSession();

        try {
            s.getAppWebSocket().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertThrows(ReceivingConnectionClosedException.class, s::assertConnected);
    }

    @Test
    void hasClosedConnections_false_when_both_open() {
        Session session = TestUtil.initSession();

        assertFalse(session.hasClosedConnections());
    }

    @Test
    void hasClosedConnections_true_when_one_closed() throws IOException {
        Session session = TestUtil.initSession();

        session.getAppWebSocket().close();

        assertTrue(session.hasClosedConnections());
    }

    @Test
    void closeConnections_closes_both_sockets() {
        Session session = TestUtil.initSession();

        session.closeConnections();

        assertFalse(session.getAppWebSocket().isOpen());
        assertFalse(session.getGisWebSocket().isOpen());
    }

    // --- getPeerConnection ---

    @Test
    void getPeerConnection_returnsOtherSide() {
        Session s = TestUtil.initSession();

        assertSame(s.getGisConnection(), s.getPeerConnection(s.getAppWebSocket()));
        assertSame(s.getAppConnection(), s.getPeerConnection(s.getGisWebSocket()));
    }

    @Test
    void getPeerConnection_returnsNullWhenIncomplete() {
        Session s = TestUtil.openSession(true);

        assertNull(s.getPeerConnection(s.getAppWebSocket()));
    }

    // --- v12Connections / hasV12Connection ---

    @Test
    void hasV12Connection_trueWhenV12Present() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        assertTrue(s.hasV12Connection());
        assertEquals(2, s.v12Connections().size());
    }

    @Test
    void hasV12Connection_falseWhenAllV1() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);

        assertFalse(s.hasV12Connection());
        assertEquals(0, s.v12Connections().size());
    }

    @Test
    void hasV12Connection_mixedProtocols() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V1);

        assertTrue(s.hasV12Connection());
        assertEquals(1, s.v12Connections().size());
    }

    // --- compareTo ---

    @Test
    @SuppressWarnings("SelfComparison")
    void compareTo_orderedBySessionNumber() {
        Sessions.resetSessionCollection();
        Session s1 = TestUtil.initSession();
        Session s2 = TestUtil.initSession();

        assertTrue(s1.compareTo(s2) < 0);
        assertTrue(s2.compareTo(s1) > 0);
        assertEquals(0, s1.compareTo(s1));
    }

    // --- equals / hashCode ---

    @Test
    void equals_reflexive() {
        Session s = TestUtil.initSession();
        assertEquals(s, s);
    }

    @Test
    void equals_sameSessionNr() {
        Sessions.resetSessionCollection();
        Session s1 = TestUtil.initSession();
        Session s2 = TestUtil.initSession();

        assertNotEquals(s1, s2);
    }

    @Test
    void equals_null_and_otherType() {
        Session s = TestUtil.initSession();
        assertNotEquals(null, s);
        assertNotEquals("not a session", s);
    }

    @Test
    void hashCode_consistentWithEquals() {
        Session s = TestUtil.initSession();
        assertEquals(s.hashCode(), s.hashCode());
    }

    // --- hasStaleClosedConnections V1.0 ---

    @Test
    void hasStaleClosedConnections_v10_immediatelyStaleWhenConnectionClosed() throws IOException {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);

        s.getAppWebSocket().close();

        assertTrue(s.hasStaleClosedConnections(Duration.ofMinutes(1)));
    }

    @Test
    void hasStaleClosedConnections_v10_notStaleWhenBothOpen() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);

        assertFalse(s.hasStaleClosedConnections(Duration.ofMinutes(1)));
    }

    // --- hasStaleClosedConnections V1.2 ---

    @Test
    void hasStaleClosedConnections_v12_notStaleWhenBothOpen() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        assertFalse(s.hasStaleClosedConnections(Duration.ofMinutes(1)));
    }

    @Test
    void hasStaleClosedConnections_v12_notStaleWithinGracePeriod() throws IOException {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        s.getAppWebSocket().close();
        s.markConnectionClosed(s.getAppWebSocket());

        assertFalse(s.hasStaleClosedConnections(Duration.ofMinutes(1)));
    }

    @Test
    void hasStaleClosedConnections_v12_staleAfterGracePeriodElapsed() throws IOException {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        s.getAppWebSocket().close();
        s.markConnectionClosed(s.getAppWebSocket());

        TestUtil.wait(150);

        assertTrue(s.hasStaleClosedConnections(Duration.ofMillis(100)));
    }

    @Test
    void hasStaleClosedConnections_v12_gisConnectionStaleAfterGracePeriod() throws IOException {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        s.getGisWebSocket().close();
        s.markConnectionClosed(s.getGisWebSocket());

        TestUtil.wait(150);

        assertTrue(s.hasStaleClosedConnections(Duration.ofMillis(100)));
    }

    // --- markConnectionClosed ---

    @Test
    void markConnectionClosed_enablesStaleDetection() throws IOException {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        s.getAppWebSocket().close();

        // Without markConnectionClosed, no timestamp is set, so not stale
        assertFalse(s.hasStaleClosedConnections(Duration.ofMillis(0)));

        s.markConnectionClosed(s.getAppWebSocket());

        // Now with timestamp set and zero grace period, should be stale
        TestUtil.wait(5);
        assertTrue(s.hasStaleClosedConnections(Duration.ofMillis(0)));
    }

    // --- clearConnectionClosedAt ---

    @Test
    void clearConnectionClosedAt_resetsTimestampAfterReconnect() throws IOException {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        s.getAppWebSocket().close();
        s.markConnectionClosed(s.getAppWebSocket());

        TestUtil.wait(150);

        // Grace period elapsed, should be stale
        assertTrue(s.hasStaleClosedConnections(Duration.ofMillis(100)));

        // Simulate reconnect: clear the timestamp
        s.clearConnectionClosedAt(true);

        // No longer stale
        assertFalse(s.hasStaleClosedConnections(Duration.ofMillis(100)));
    }

    @Test
    void clearConnectionClosedAt_resetsGisTimestamp() throws IOException {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        s.getGisWebSocket().close();
        s.markConnectionClosed(s.getGisWebSocket());

        TestUtil.wait(150);

        assertTrue(s.hasStaleClosedConnections(Duration.ofMillis(100)));

        s.clearConnectionClosedAt(false);

        assertFalse(s.hasStaleClosedConnections(Duration.ofMillis(100)));
    }
}
