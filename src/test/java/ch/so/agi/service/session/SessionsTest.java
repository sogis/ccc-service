package ch.so.agi.service.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SessionsTest {

    @BeforeEach
    void resetSessions() throws Exception {
        Field field = Sessions.class.getDeclaredField("sessionsBySocket");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<WebSocketSession, Session> sessions = (Map<WebSocketSession, Session>) field.get(null);
        sessions.clear();
    }

    @Test
    void findBySessionReturnsMatchingSession() {
        UUID sessionUid = UUID.randomUUID();
        WebSocketSession appSocket = mock(WebSocketSession.class);
        SockConnection appConnection = new SockConnection("app-client", "1.0", appSocket);
        Session session = new Session(sessionUid, appConnection, true);

        Sessions.add(session);

        Session found = Sessions.findBySession(sessionUid);

        assertNotNull(found);
        assertSame(session, found);
    }

    @Test
    void findBySessionReturnsNullWhenNotFound() {
        UUID sessionUid = UUID.randomUUID();

        Session found = Sessions.findBySession(sessionUid);

        assertNull(found);
    }

    @Test
    void findBySessionReturnsNullWhenUuidIsNull() {
        Session found = Sessions.findBySession(null);

        assertNull(found);
    }
}
