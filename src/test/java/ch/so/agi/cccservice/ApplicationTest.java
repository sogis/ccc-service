package ch.so.agi.cccservice.health;

import ch.so.agi.cccservice.CCCWebSocketHandler;
import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.WebSocketConfig;
import ch.so.agi.cccservice.deamon.KeyChanger;
import ch.so.agi.cccservice.deamon.SessionsKiller;
import ch.so.agi.cccservice.deamon.PingSender;
import ch.so.agi.cccservice.deamon.SessionsGroomer;
import ch.so.agi.cccservice.session.Sessions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

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

        TestUtil.wait(CCCWebSocketHandler.CONNECT_MSG_MAX_DELAY_SECONDS * 1000 + 100);

        assertFalse(c.webSocketIsOpen());
    }

    private static List<TestClient> createClients(int numClients){
        List<TestClient> clients = IntStream.range(0, numClients)
                .mapToObj(i -> new TestClient())
                .collect(Collectors.toList());
        return clients;
    }
}