package ch.so.agi.cccservice.deamon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;

class KillSessionsTest {

    @BeforeEach
    void reset(){
        Sessions.resetSessionCollection();
    }

    @Test
    void killOnEmptyCollection_OK(){
        int closed = Sessions.resetSessionCollection();
        assertEquals(0, closed);
    }

    @Test
    void handshakeAfterReset_OK(){
        Sessions.resetSessionCollection();

        TestUtil.initSession();
        TestUtil.initSession();

        assertEquals(2, Sessions.allSessions().count());
    }

    @Test
    void returnedSessionCount_OK(){
        TestUtil.initSession();
        TestUtil.initSession();

        int closedSessions = Sessions.resetSessionCollection();

        assertEquals(2, closedSessions);
    }

    @Test
    void sessionNumbersRestartAfterReset_OK(){
        TestUtil.initSession();
        TestUtil.initSession();

        Sessions.resetSessionCollection();

        Session s = TestUtil.initSession();
        assertEquals(1, s.getSessionNr());
    }

    @Test
    void collectionEmptyAfterReset_OK(){
        TestUtil.initSession();
        TestUtil.initSession();

        Sessions.resetSessionCollection();

        assertEquals(0, Sessions.allSessions().count());
    }

    @Test
    void connectionsClosedAfterReset_OK(){
        Session s1 = TestUtil.initSession();
        Session s2 = TestUtil.initSession();

        Sessions.resetSessionCollection();

        assertFalse(s1.getAppWebSocket().isOpen());
        assertFalse(s1.getGisWebSocket().isOpen());
        assertFalse(s2.getAppWebSocket().isOpen());
        assertFalse(s2.getGisWebSocket().isOpen());
    }
}