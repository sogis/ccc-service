package ch.so.agi.service.deamon;

import ch.so.agi.service.TestUtil;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SessionCleanupTest {
    @BeforeEach
    void resetSessions() throws Exception {
        Sessions.removeAll();
    }

    @Test
    void removeStaleSessionsHandlesEmptyCollection() {
        assertEquals(0, Sessions.allSessions().count());

        (new SessionCleanup()).removeStaleSessions();

        assertEquals(0, Sessions.allSessions().count());
    }

    @Test
    void removeStaleSessionsRemovesAllStaleEntries() throws IOException {
        TestUtil.initSession().getAppWebSocket().close();
        TestUtil.initSession().getGisWebSocket().close();

        assertEquals(2, Sessions.allSessions().count());

        (new SessionCleanup()).removeStaleSessions();

        assertEquals(0, Sessions.allSessions().count());
    }

    @Test
    void removeStaleSessionsOnlyRemovesStaleEntries() throws IOException {
        TestUtil.initSession();
        TestUtil.initSession().getGisWebSocket().close();

        assertEquals(2, Sessions.allSessions().count());

        (new SessionCleanup()).removeStaleSessions();

        assertEquals(1, Sessions.allSessions().count());
    }
}