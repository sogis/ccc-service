package ch.so.agi.cccservice.http;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;

class StatusPageTest {

    @BeforeEach
    void setUp() {
        Sessions.resetSessionCollection();
    }

    @Test
    void statusInfo_containsVersionFromBuildProperties() {
        Properties props = new Properties();
        props.setProperty("version", "1.2.2.42");
        StatusPage page = new StatusPage(new BuildProperties(props));

        String result = page.statusInfo();

        assertContains(result, "CCC-Service Version 1.2.2.42");
        assertContains(result, "Sessions total: 0");
    }

    @Test
    void noSessions_allCountsZero() {
        String result = Sessions.sessionStats();

        assertContains(result, "Sessions total: 0");
        assertContains(result, "open: 0");
        assertContains(result, "waiting for reconnect: 0");
        assertContains(result, "partial: 0");
    }

    @Test
    void oneOpenSession_countedAsOpen() {
        TestUtil.initSession();

        String result = Sessions.sessionStats();

        assertContains(result, "Sessions total: 1");
        assertContains(result, "open: 1");
        assertContains(result, "waiting for reconnect: 0");
        assertContains(result, "partial: 0");
    }

    @Test
    void v12SessionWithClosedConnection_countedAsWaitingForReconnect() throws IOException {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        s.getAppWebSocket().close();

        String result = Sessions.sessionStats();

        assertContains(result, "Sessions total: 1");
        assertContains(result, "open: 0");
        assertContains(result, "waiting for reconnect: 1");
        assertContains(result, "partial: 0");
    }

    @Test
    void sessionWithMissingConnection_countedAsPartial() {
        TestUtil.openSession(true); // nur App verbunden, GIS fehlt

        String result = Sessions.sessionStats();

        assertContains(result, "Sessions total: 1");
        assertContains(result, "open: 0");
        assertContains(result, "waiting for reconnect: 0");
        assertContains(result, "partial: 1");
    }

    @Test
    void mixedSessions_allCategoriesCorrect() throws IOException {
        TestUtil.initSession();

        Session waitingSession = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        waitingSession.getAppWebSocket().close();

        TestUtil.openSession(true);

        String result = Sessions.sessionStats();

        assertContains(result, "Sessions total: 3");
        assertContains(result, "open: 1");
        assertContains(result, "waiting for reconnect: 1");
        assertContains(result, "partial: 1");
    }

    private static void assertContains(String actual, String expected) {
        assertTrue(actual.contains(expected),
                "Expected session stats to contain \"%s\", but got:%n%s".formatted(expected, actual));
    }
}
