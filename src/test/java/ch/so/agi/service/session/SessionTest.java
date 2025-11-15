package ch.so.agi.service.session;

import ch.so.agi.service.TestUtil;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @Test
    void secondConnectionAddedWithinHandshakeWindow() {
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

        assertThrows(RuntimeException.class, session::assertConnected);
    }

    @Test
    void assertConnected_fails_on_closed_socket() {
        Session s = TestUtil.initSession();

        try {
            s.getAppWebSocket().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertThrows(RuntimeException.class, s::assertConnected);
    }
}
