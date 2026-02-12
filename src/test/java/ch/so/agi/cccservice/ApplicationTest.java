package ch.so.agi.cccservice.health;

import ch.so.agi.cccservice.CCCWebSocketHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.WebSocketConfig;
import ch.so.agi.cccservice.deamon.KeyChanger;
import ch.so.agi.cccservice.deamon.SessionsKiller;
import ch.so.agi.cccservice.deamon.PingSender;
import ch.so.agi.cccservice.deamon.SessionsGroomer;
import ch.so.agi.cccservice.security.ConnectionLimiter;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;

import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationTest {

    @Autowired
    private KeyChanger keyChanger;

    @Autowired
    private PingSender pinger;

    @Autowired
    private SessionsGroomer cleaner;

    @Autowired
    private SessionsKiller killer;

    @Autowired
    private ReadinessProbe readinessProbe;

    @Value("${ccc.websocket.connect-msg-max-delay-seconds:" + CCCWebSocketHandler.DEFAULT_CONNECT_MSG_MAX_DELAY_SECONDS + "}")
    private int connectMsgMaxDelaySeconds;

    @BeforeEach
    void resetConnectionLimiter() {
        ConnectionLimiter.getInstance().reset();
    }

    @Test
    void testClient_reconnectAndSend_WorksTwice(){
        assertDoesNotThrow(
                () -> {
                    TestClient t = new TestClient();
                    t.reconnectAndSend();
                    t.reconnectAndSend();
                }
        );
    }

    /**
     * Tests whether a TestClient instance can still send after the service reset all sessions.
     * This test provokes a logged null pointer exception due to parallelism of the service killing
     * all sessions and the client trying to send messages at the same time.
     * The cause of the null pointer is not corrected, as no additional time is spent on a clean
     * testclient implementation.
     */
    @Test
    void testClient_canSendAfter_sessionKill(){
        assertDoesNotThrow(
                () -> {
                    TestClient c = new TestClient();
                    c.reconnectAndSend();

                    killer.killAllSessions();

                    c.reconnectAndSend();
                }
        );
    }

    /**
     * Tests whether client connections survive
     * the run of all deamon services except the killer service.
     */
    @Test
    void connections_stayValid_after_deamons(){
        Sessions.resetSessionCollection();
        int numClients = 5;

        assertDoesNotThrow(
                () -> {
                    List<TestClient> clients = createClients(numClients);
                    clients.forEach(TestClient::reconnectAndSend);

                    clients.getFirst().closeConnection(true);
                    clients.getLast().closeConnection(false);

                    keyChanger.sendKeyChange();
                    pinger.pingConnections();
                    cleaner.removeStaleSessions();
                }
        );

        int numOpenClients = numClients - 2;
        assertEquals(numOpenClients, Sessions.openSessions().size());
    }

    @Test
    void missingConnectMsg_closesConnection(){
        String adr = "ws://localhost:" + WebServerPort.getPort() + WebSocketConfig.CCC_SOCKET_PATH;
        SocketClient c = new SocketClient(adr, SocketClient.ClientType.APP);
        c.connectWebSocket();

        Awaitility.await()
                .atMost(connectMsgMaxDelaySeconds + 2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertFalse(c.webSocketIsOpen()));
    }

    @Test
    void readinessProbe_concurrent_allHealthy() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Future<Health>> futures = IntStream.range(0, threadCount)
                .mapToObj(i -> executor.submit(() -> readinessProbe.health()))
                .collect(Collectors.toList());

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        for (Future<Health> future : futures) {
            assertEquals(Status.UP, future.get().getStatus());
        }
    }

    @Test
    void largeMessage_isForwardedWithoutDisconnect() {
        String adr = "ws://localhost:" + WebServerPort.getPort() + WebSocketConfig.CCC_SOCKET_PATH;
        UUID sesUid = UUID.randomUUID();

        int largeBuffer = 2 * 1024 * 1024;
        SocketClient appClient = new SocketClient(adr, SocketClient.ClientType.APP, largeBuffer);
        SocketClient gisClient = new SocketClient(adr, SocketClient.ClientType.GIS, largeBuffer);

        appClient.connectCCC(sesUid, "test-app", SockConnection.PROTOCOL_V12, SocketClient.ClientType.APP);
        gisClient.connectCCC(sesUid, "test-gis", SockConnection.PROTOCOL_V12, SocketClient.ClientType.GIS);

        // Wait for handshake
        Awaitility.await().atMost(2, TimeUnit.SECONDS)
                .until(() -> appClient.getSessionNr() != null && gisClient.getSessionNr() != null);

        // Build a showGeoObject message >100KB (exceeds maxTextMessageBufferSize)
        StringBuilder coords = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            if (i > 0) coords.append(",");
            coords.append("[260").append(7000 + i).append(".123,122").append(9000 + i).append(".456]");
        }
        String largeMessage = String.format(
                "{\"method\":\"showGeoObject\",\"context\":{},\"data\":{\"type\":\"Polygon\",\"coordinates\":[[%s]]}}",
                coords.toString());

        assertTrue(largeMessage.length() > 102400, "Message must exceed buffer size");

        appClient.sendRawMessage(largeMessage);

        // GIS client should receive the forwarded message
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> "showGeoObject".equals(gisClient.getLastReceivedMethod()));

        assertTrue(appClient.webSocketIsOpen(), "App connection must stay open");
        assertTrue(gisClient.webSocketIsOpen(), "GIS connection must stay open");
    }

    private static List<TestClient> createClients(int numClients){
        List<TestClient> clients = IntStream.range(0, numClients)
                .mapToObj(i -> new TestClient())
                .collect(Collectors.toList());
        return clients;
    }
}