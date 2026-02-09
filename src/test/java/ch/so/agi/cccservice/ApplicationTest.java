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
    private LivenessProbe livenessProbe;

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
    void livenessProbe_concurrent_allHealthy() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Future<Health>> futures = IntStream.range(0, threadCount)
                .mapToObj(i -> executor.submit(() -> livenessProbe.health()))
                .collect(Collectors.toList());

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        for (Future<Health> future : futures) {
            assertEquals(Status.UP, future.get().getStatus());
        }
    }

    private static List<TestClient> createClients(int numClients){
        List<TestClient> clients = IntStream.range(0, numClients)
                .mapToObj(i -> new TestClient())
                .collect(Collectors.toList());
        return clients;
    }
}