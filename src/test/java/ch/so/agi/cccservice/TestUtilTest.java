package ch.so.agi.cccservice;

import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TestUtilTest {

    @Test
    void testWait() {
        LocalDateTime start = LocalDateTime.now();
        TestUtil.wait(100);
        LocalDateTime end = LocalDateTime.now();
        int effectiveWait = (int) Duration.between(start, end).toMillis();
        assertTrue(effectiveWait >= 100);
    }

    @Test
    void initSession() {
        Session created = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V12);

        assertNotNull(created, "Returned session is null");
        assertEquals(SockConnection.PROTOCOL_V1, created.getAppConnection().getApiVersion());
        assertEquals(SockConnection.PROTOCOL_V12, created.getGisConnection().getApiVersion());

        assertNotNull(Sessions.findByConnection(created.getAppWebSocket()), "Session must be registered by its app connection in the Sessions collection");
        assertNotNull(Sessions.findByConnection(created.getGisWebSocket()), "Session must be registered by its gis connection in the Sessions collection");
    }
}