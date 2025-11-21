package ch.so.agi.service.session;

import ch.so.agi.service.TestUtil;
import ch.so.agi.service.exception.HandshakeIncompleteException;
import ch.so.agi.service.exception.ReceivingConnectionClosedException;
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
}
