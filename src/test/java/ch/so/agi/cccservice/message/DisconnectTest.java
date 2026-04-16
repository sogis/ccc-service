package ch.so.agi.cccservice.message;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.so.agi.cccservice.MessageHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;

class DisconnectTest {

    @BeforeEach
    void reset() {
        Sessions.resetSessionCollection();
    }

    @Test
    void disconnectApp_removesSession() {
        UUID sessionId = UUID.randomUUID();
        Session s = TestUtil.initSession(sessionId, SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        MockWebSocketSession appSocket = (MockWebSocketSession) s.getAppWebSocket();
        MessageHandler.handleMessage(appSocket, disconnectMessage("disconnectApp"));

        assertNull(Sessions.findBySessionUid(sessionId), "Session should be removed after disconnect");
    }

    @Test
    void disconnectGis_removesSession() {
        UUID sessionId = UUID.randomUUID();
        Session s = TestUtil.initSession(sessionId, SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        MockWebSocketSession gisSocket = (MockWebSocketSession) s.getGisWebSocket();
        MessageHandler.handleMessage(gisSocket, disconnectMessage("disconnectGis"));

        assertNull(Sessions.findBySessionUid(sessionId), "Session should be removed after disconnect");
    }

    @Test
    void disconnectApp_closesBothConnections() {
        UUID sessionId = UUID.randomUUID();
        Session s = TestUtil.initSession(sessionId, SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        MockWebSocketSession appSocket = (MockWebSocketSession) s.getAppWebSocket();
        MockWebSocketSession gisSocket = (MockWebSocketSession) s.getGisWebSocket();

        MessageHandler.handleMessage(appSocket, disconnectMessage("disconnectApp"));

        assertFalse(appSocket.isOpen(), "App connection should be closed");
        assertFalse(gisSocket.isOpen(), "Gis connection should also be closed");
    }

    @Test
    void disconnect_unknownConnection_isIgnored() {
        MockWebSocketSession unknownSocket = new MockWebSocketSession();
        // Should not throw
        MessageHandler.handleMessage(unknownSocket, disconnectMessage("disconnectApp"));
    }

    @Test
    void disconnect_v10Session_removesSession() {
        UUID sessionId = UUID.randomUUID();
        Session s = TestUtil.initSession(sessionId);

        MockWebSocketSession appSocket = (MockWebSocketSession) s.getAppWebSocket();
        MessageHandler.handleMessage(appSocket, disconnectMessage("disconnectApp"));

        assertNull(Sessions.findBySessionUid(sessionId), "V1.0 session should also be removed after disconnect");
    }

    @Test
    void disconnect_otherSessionsUnaffected() {
        UUID sessionId1 = UUID.randomUUID();
        UUID sessionId2 = UUID.randomUUID();
        Session s1 = TestUtil.initSession(sessionId1, SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        Session s2 = TestUtil.initSession(sessionId2, SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        MockWebSocketSession appSocket1 = (MockWebSocketSession) s1.getAppWebSocket();
        MessageHandler.handleMessage(appSocket1, disconnectMessage("disconnectApp"));

        assertNull(Sessions.findBySessionUid(sessionId1), "Disconnected session should be removed");
        assertTrue(s2.getAppWebSocket().isOpen(), "Other session's app connection should remain open");
        assertTrue(s2.getGisWebSocket().isOpen(), "Other session's gis connection should remain open");
    }

    private String disconnectMessage(String method) {
        return """
                {
                    "method": "%s"
                }
                """.formatted(method);
    }
}
