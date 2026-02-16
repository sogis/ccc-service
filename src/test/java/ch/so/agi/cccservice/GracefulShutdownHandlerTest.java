package ch.so.agi.cccservice;

import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GracefulShutdownHandlerTest {

    private GracefulShutdownHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GracefulShutdownHandler();
        Sessions.resetSessionCollection();
    }

    @AfterEach
    void tearDown() {
        Sessions.resetSessionCollection();
    }

    @Test
    void onShutdown_closesAllOpenSessions() {
        // Arrange
        MockWebSocketSession appSocket = MockWebSocketSession.create();
        MockWebSocketSession gisSocket = MockWebSocketSession.create();

        SockConnection appConnection = new SockConnection("testApp", SockConnection.PROTOCOL_V12, appSocket);
        SockConnection gisConnection = new SockConnection("testGis", SockConnection.PROTOCOL_V12, gisSocket);

        UUID sessionUid = UUID.randomUUID();
        Session session = new Session(sessionUid, appConnection, true);
        session.tryToAddSecondConnection(gisConnection, false);
        Sessions.addOrReplace(session);

        assertTrue(appSocket.isOpen(), "App socket should be open before shutdown");
        assertTrue(gisSocket.isOpen(), "GIS socket should be open before shutdown");

        // Act
        handler.onShutdown();

        // Assert
        assertFalse(appSocket.isOpen(), "App socket should be closed after shutdown");
        assertFalse(gisSocket.isOpen(), "GIS socket should be closed after shutdown");
    }

    @Test
    void onShutdown_handlesClosedSessions() throws Exception {
        // Arrange
        MockWebSocketSession appSocket = MockWebSocketSession.create();
        MockWebSocketSession gisSocket = MockWebSocketSession.create();

        SockConnection appConnection = new SockConnection("testApp", SockConnection.PROTOCOL_V12, appSocket);
        SockConnection gisConnection = new SockConnection("testGis", SockConnection.PROTOCOL_V12, gisSocket);

        UUID sessionUid = UUID.randomUUID();
        Session session = new Session(sessionUid, appConnection, true);
        session.tryToAddSecondConnection(gisConnection, false);
        Sessions.addOrReplace(session);

        // Close sockets before shutdown
        appSocket.close();
        gisSocket.close();

        assertFalse(appSocket.isOpen(), "App socket should be closed before shutdown");
        assertFalse(gisSocket.isOpen(), "GIS socket should be closed before shutdown");

        // Act & Assert - should not throw exceptions for already closed sockets
        assertDoesNotThrow(() -> handler.onShutdown());
    }

    @Test
    void onShutdown_withNoSessions_completesWithoutError() {
        // Act & Assert - should not throw
        assertDoesNotThrow(() -> handler.onShutdown());
    }

    @Test
    void onShutdown_withMultipleSessions_closesAll() {
        // Arrange - Create 3 sessions
        for (int i = 0; i < 3; i++) {
            MockWebSocketSession appSocket = MockWebSocketSession.create();
            MockWebSocketSession gisSocket = MockWebSocketSession.create();

            SockConnection appConnection = new SockConnection("app" + i, SockConnection.PROTOCOL_V12, appSocket);
            SockConnection gisConnection = new SockConnection("gis" + i, SockConnection.PROTOCOL_V12, gisSocket);

            UUID sessionUid = UUID.randomUUID();
            Session session = new Session(sessionUid, appConnection, true);
            session.tryToAddSecondConnection(gisConnection, false);
            Sessions.addOrReplace(session);
        }

        assertEquals(3, Sessions.openSessions().size(), "Should have 3 open sessions before shutdown");

        // Act
        handler.onShutdown();

        // Assert - all sessions should be closed
        assertEquals(0, Sessions.openSessions().size(), "Should have 0 open sessions after shutdown");
    }
}
