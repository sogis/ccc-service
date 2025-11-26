package ch.so.agi.cccservice.deamon;

import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.session.Sessions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
}