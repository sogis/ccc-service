package ch.so.agi.cccservice.message;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.so.agi.cccservice.MessageHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.exception.ForbiddenReconnectException;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;

class ReconnectTest {

    @BeforeEach
    void reset() {
        Sessions.resetSessionCollection();
    }

    @Test
    void reconnectApp_validKey_switchesConnection() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        String oldKey = s.getAppConnection().getConnectionKey();

        MockWebSocketSession newAppSocket = new MockWebSocketSession();
        String reconnectMsg = reconnectMessage("reconnectApp", oldKey, s.getSessionNr());
        MessageHandler.handleMessage(newAppSocket, reconnectMsg);

        assertSame(newAppSocket, s.getAppWebSocket());
    }

    @Test
    void reconnectGis_validKey_switchesConnection() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        String oldKey = s.getGisConnection().getConnectionKey();

        MockWebSocketSession newGisSocket = new MockWebSocketSession();
        String reconnectMsg = reconnectMessage("reconnectGis", oldKey, s.getSessionNr());
        MessageHandler.handleMessage(newGisSocket, reconnectMsg);

        assertSame(newGisSocket, s.getGisWebSocket());
    }

    @Test
    void reconnect_invalidKey_sendsError() {
        TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        MockWebSocketSession newSocket = new MockWebSocketSession();
        String reconnectMsg = reconnectMessage("reconnectApp", "invalidKey", 1);
        MessageHandler.handleMessage(newSocket, reconnectMsg);

        String sent = newSocket.getLastSentTextMessage();
        assertNotNull(sent);
        assertTrue(sent.contains("notifyError"));
        assertTrue(sent.contains("invalidKey"));
    }

    @Test
    void reconnect_v1Client_throwsForbiddenReconnect() {
        Session s = TestUtil.initSession();
        String oldKey = s.getAppConnection().getConnectionKey();

        String reconnectMsg = reconnectMessage("reconnectApp", oldKey, s.getSessionNr());
        Message msg = Message.forJsonString(reconnectMsg);

        MockWebSocketSession newSocket = new MockWebSocketSession();
        assertThrows(ForbiddenReconnectException.class, () -> msg.process(newSocket));
    }

    private String reconnectMessage(String method, String oldConnectionKey, int oldSessionNumber) {
        return """
                {
                    "method": "$METHOD",
                    "oldConnectionKey": "$KEY",
                    "oldSessionNumber": $NUMBER
                }
                """
                .replace("$METHOD", method)
                .replace("$KEY", oldConnectionKey)
                .replace("$NUMBER", String.valueOf(oldSessionNumber));
    }
}
