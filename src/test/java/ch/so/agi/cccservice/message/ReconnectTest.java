package ch.so.agi.cccservice.message;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertKeyChangeSent(newAppSocket);
    }

    @Test
    void reconnectGis_validKey_switchesConnection() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        String oldKey = s.getGisConnection().getConnectionKey();

        MockWebSocketSession newGisSocket = new MockWebSocketSession();
        String reconnectMsg = reconnectMessage("reconnectGis", oldKey, s.getSessionNr());
        MessageHandler.handleMessage(newGisSocket, reconnectMsg);

        assertSame(newGisSocket, s.getGisWebSocket());
        assertKeyChangeSent(newGisSocket);
    }

    @Test
    void reconnect_validKey_sendsKeyChange() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        String oldKey = s.getAppConnection().getConnectionKey();

        MockWebSocketSession newSocket = new MockWebSocketSession();
        MessageHandler.handleMessage(newSocket, reconnectMessage("reconnectApp", oldKey, s.getSessionNr()));

        String sent = newSocket.getLastSentTextMessage();
        assertNotNull(sent, "Expected keyChange message after reconnect");
        assertTrue(sent.contains("\"method\": \"keyChange\""), "Expected keyChange method in message");
        assertTrue(sent.contains("\"newConnectionKey\""), "Expected newConnectionKey in message");
    }

    @Test
    void reconnect_validKey_updatesSessionMap() {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        String oldKey = s.getAppConnection().getConnectionKey();

        MockWebSocketSession newSocket = new MockWebSocketSession();
        MessageHandler.handleMessage(newSocket, reconnectMessage("reconnectApp", oldKey, s.getSessionNr()));

        Session found = Sessions.findByConnection(newSocket);
        assertNotNull(found, "Session should be findable by the new WebSocket after reconnect");
        assertEquals(s.getSessionNr(), found.getSessionNr());
    }

    @Test
    void reconnect_invalidKey_sendsError() {
        TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        MockWebSocketSession newSocket = new MockWebSocketSession();
        String reconnectMsg = reconnectMessage("reconnectApp", "invalidKey", 1);
        MessageHandler.handleMessage(newSocket, reconnectMsg);

        String sent = newSocket.getLastSentTextMessage();
        assertNotNull(sent);
        assertTrue(sent.contains(ErrorMessage.MESSAGE_TYPE));
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

    private void assertKeyChangeSent(MockWebSocketSession socket) {
        String sent = socket.getLastSentTextMessage();
        assertNotNull(sent, "Expected keyChange message after reconnect");
        assertTrue(sent.contains(KeyChange.METHOD_TYPE), "Expected keyChange in sent message");
    }

    @Test
    void simultaneousReconnects_bothClientsSucceed() throws Exception {
        UUID sessionId = UUID.randomUUID();
        Session session = TestUtil.initSession(sessionId, SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);

        String appKey = session.getAppConnection().getConnectionKey();
        String gisKey = session.getGisConnection().getConnectionKey();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Beide Clients reconnecten gleichzeitig
        MockWebSocketSession newApp = new MockWebSocketSession();
        MockWebSocketSession newGis = new MockWebSocketSession();

        Future<?> appReconnect = executor.submit(() ->
            MessageHandler.handleMessage(newApp,
                reconnectMessage("reconnectApp", appKey, session.getSessionNr())));

        Future<?> gisReconnect = executor.submit(() ->
            MessageHandler.handleMessage(newGis,
                reconnectMessage("reconnectGis", gisKey, session.getSessionNr())));

        appReconnect.get();
        gisReconnect.get();

        assertSame(newApp, session.getAppWebSocket());
        assertSame(newGis, session.getGisWebSocket());

        executor.shutdown();
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
