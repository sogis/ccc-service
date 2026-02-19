package ch.so.agi.cccservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;

import ch.so.agi.cccservice.message.MessageAccumulator;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.SockConnection;
import ch.so.agi.cccservice.session.Sessions;

class CCCWebSocketHandlerTest {

    private CCCWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        Sessions.resetSessionCollection();
        handler = new CCCWebSocketHandler(new MessageAccumulator(), CCCWebSocketHandler.DEFAULT_CONNECT_MSG_MAX_DELAY_SECONDS, true, true);
    }

    @Test
    void noConnectMessage_connectionClosedAfterDelay() throws Exception {
        MockWebSocketSession socket = new MockWebSocketSession();

        handler.afterConnectionEstablished(socket);

        // Kein Connect innerhalb DEFAULT_CONNECT_MSG_MAX_DELAY_SECONDS → Verbindung wird geschlossen
        Awaitility.await()
                .atMost(CCCWebSocketHandler.DEFAULT_CONNECT_MSG_MAX_DELAY_SECONDS + 2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertFalse(socket.isOpen()));
    }

    @Test
    void connectMessageSent_connectionStaysOpen() throws Exception {
        UUID sessionUid = UUID.randomUUID();
        MockWebSocketSession socket = new MockWebSocketSession();

        handler.afterConnectionEstablished(socket);

        String connectMsg = """
                {
                    "method": "connectApp",
                    "clientName": "TestApp",
                    "apiVersion": "1.0",
                    "session": "{$SESSION}"
                }
                """.replace("$SESSION", sessionUid.toString());
        MessageHandler.handleMessage(socket, connectMsg);

        TestUtil.wait(3000);

        // Connect-Message gesendet → Verbindung bleibt offen
        assertTrue(socket.isOpen());
    }

    // --- V1.0: sofortiger Abbruch bei WebSocket close ---

    @Test
    void v10_appConnectionClose_sessionRemovedAndPeerClosed() throws Exception {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);
        Sessions.addOrReplace(s);
        MockWebSocketSession appSocket = (MockWebSocketSession) s.getAppWebSocket();

        handler.afterConnectionClosed(appSocket, CloseStatus.NORMAL);

        assertEquals(0, Sessions.allSessions().count(), "V1.0 session should be removed immediately");
        assertFalse(s.getGisWebSocket().isOpen(), "Peer connection should be closed");
    }

    @Test
    void v10_gisConnectionClose_sessionRemovedAndPeerClosed() throws Exception {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);
        Sessions.addOrReplace(s);
        MockWebSocketSession gisSocket = (MockWebSocketSession) s.getGisWebSocket();

        handler.afterConnectionClosed(gisSocket, CloseStatus.NORMAL);

        assertFalse(s.getAppWebSocket().isOpen(), "App connection should be closed when GIS disconnects");
    }

    @Test
    void afterConnectionClosed_noSessionExists_noException() throws Exception {
        MockWebSocketSession socket = new MockWebSocketSession();

        handler.afterConnectionClosed(socket, CloseStatus.NORMAL);

        assertEquals(0, Sessions.allSessions().count());
    }

    // --- V1.2: Session bleibt bestehen bei WebSocket close ---

    @Test
    void v12_sessionSurvivesConnectionClose() throws Exception {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V12);
        Sessions.addOrReplace(s);
        MockWebSocketSession appSocket = (MockWebSocketSession) s.getAppWebSocket();

        handler.afterConnectionClosed(appSocket, CloseStatus.NORMAL);

        assertEquals(1, Sessions.allSessions().count(), "V1.2 session should survive for reconnect");
        assertTrue(s.getGisWebSocket().isOpen(), "Peer connection should remain open");
    }

    // --- Mixed V1.0/V1.2: V1.0 connection close terminates session ---

    @Test
    void mixed_v10AppClose_sessionTerminatedImmediately() throws Exception {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V12);
        Sessions.addOrReplace(s);
        MockWebSocketSession appSocket = (MockWebSocketSession) s.getAppWebSocket();

        handler.afterConnectionClosed(appSocket, CloseStatus.NORMAL);

        assertEquals(0, Sessions.allSessions().count(), "Session with V1.0 App should be terminated immediately");
        assertFalse(s.getGisWebSocket().isOpen(), "Peer (V1.2 GIS) connection should be closed");
    }

    @Test
    void mixed_v10GisClose_sessionTerminatedImmediately() throws Exception {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V1);
        Sessions.addOrReplace(s);
        MockWebSocketSession gisSocket = (MockWebSocketSession) s.getGisWebSocket();

        handler.afterConnectionClosed(gisSocket, CloseStatus.NORMAL);

        assertEquals(0, Sessions.allSessions().count(), "Session with V1.0 GIS should be terminated immediately");
        assertFalse(s.getAppWebSocket().isOpen(), "Peer (V1.2 App) connection should be closed");
    }

    @Test
    void v10_bothConnectionsCloseConcurrently_noExceptionAndSessionRemoved() throws Exception {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);
        Sessions.addOrReplace(s);
        MockWebSocketSession appSocket = (MockWebSocketSession) s.getAppWebSocket();
        MockWebSocketSession gisSocket = (MockWebSocketSession) s.getGisWebSocket();

        CyclicBarrier barrier = new CyclicBarrier(2);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        Thread appThread = new Thread(() -> {
            try {
                barrier.await();
                handler.afterConnectionClosed(appSocket, CloseStatus.NORMAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exceptions.add(e);
            } catch (BrokenBarrierException e) {
                exceptions.add(e);
            }
        });
        Thread gisThread = new Thread(() -> {
            try {
                barrier.await();
                handler.afterConnectionClosed(gisSocket, CloseStatus.NORMAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exceptions.add(e);
            } catch (BrokenBarrierException e) {
                exceptions.add(e);
            }
        });

        appThread.start();
        gisThread.start();
        appThread.join();
        gisThread.join();

        assertTrue(exceptions.isEmpty(), "No exceptions should be thrown during concurrent close: " + exceptions);
        assertEquals(0, Sessions.allSessions().count(), "Session should be removed exactly once");
    }

    @Test
    void mixed_v12AppClose_sessionSurvives() throws Exception {
        Session s = TestUtil.initSession(UUID.randomUUID(), SockConnection.PROTOCOL_V12, SockConnection.PROTOCOL_V1);
        Sessions.addOrReplace(s);
        MockWebSocketSession appSocket = (MockWebSocketSession) s.getAppWebSocket();

        handler.afterConnectionClosed(appSocket, CloseStatus.NORMAL);

        assertEquals(1, Sessions.allSessions().count(), "V1.2 App close should allow reconnect");
        assertTrue(s.getGisWebSocket().isOpen(), "V1.0 GIS connection should remain open");
    }
}
