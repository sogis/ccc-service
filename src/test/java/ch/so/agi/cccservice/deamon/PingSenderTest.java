package ch.so.agi.cccservice.deamon;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;

class PingSenderTest {
    @BeforeEach
    void resetSessions() throws Exception {
        Sessions.resetSessionCollection();
    }

    @Test
    void pingHandlesEmptyCollection() {
        assertEquals(0, Sessions.allSessions().count());

        assertDoesNotThrow(
                () -> new PingSender().pingConnections()
        );
    }

    @Test
    void pingOnlySentToOpenSessions() throws IOException {
        TestUtil.initSession(); // Full open
        TestUtil.initSession().getGisWebSocket().close(); // partially open to app
        TestUtil.initSession().getAppWebSocket().close(); // partially open to gis

        Session bothClosed = TestUtil.initSession();
        bothClosed.getGisWebSocket().close();
        bothClosed.getAppWebSocket().close();

        int pingedSessions = new PingSender().pingConnections();

        assertEquals(1, pingedSessions);
    }

    @Test
    void pingContinuesForRemainingSessions_afterOneConnectionFails() throws IOException {
        Session failing = TestUtil.initSession();
        ((MockWebSocketSession) failing.getAppWebSocket())
                .failSendsWith(new RuntimeException("simulated send failure"));

        TestUtil.initSession(); // healthy session, must still get pinged

        int pingedSessions = assertDoesNotThrow(
                () -> new PingSender().pingConnections()
        );

        assertEquals(2, pingedSessions);
    }
}

