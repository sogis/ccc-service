package ch.so.agi.service.session;

import org.junit.jupiter.api.Test;

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
        Session session = new Session(sessionUid, gisConnection, false, Duration.ofSeconds(5));

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
        Duration handshakeDuration = Duration.ofSeconds(1);
        MockWebSocketSession gisWebSocket = new MockWebSocketSession();
        SockConnection gisConnection = new SockConnection("gis-client", "1.0", gisWebSocket);
        Session session = new Session(sessionUid, gisConnection, false, handshakeDuration);

        Field initializedField = Session.class.getDeclaredField("handShakeInitialized");
        initializedField.setAccessible(true);
        initializedField.set(session, LocalDateTime.now().minus(handshakeDuration).minusSeconds(1));

        MockWebSocketSession appWebSocket = new MockWebSocketSession();
        SockConnection appConnection = new SockConnection("app-client", "1.0", appWebSocket);

        boolean added = session.tryToAddSecondConnection(appConnection, true);

        assertFalse(added);
        assertNull(session.getAppWebSocket());
        assertSame(gisWebSocket, session.getGisWebSocket());
    }
}
