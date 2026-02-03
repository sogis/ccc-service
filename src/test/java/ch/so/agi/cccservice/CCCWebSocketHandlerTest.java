package ch.so.agi.cccservice;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.so.agi.cccservice.message.MessageAccumulator;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Sessions;

class CCCWebSocketHandlerTest {

    private CCCWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        Sessions.resetSessionCollection();
        handler = new CCCWebSocketHandler(new MessageAccumulator());
    }

    @Test
    void noConnectMessage_connectionClosedAfterDelay() throws Exception {
        MockWebSocketSession socket = new MockWebSocketSession();

        handler.afterConnectionEstablished(socket);

        // Kein Connect innerhalb 2 Sek. → Verbindung wird geschlossen
        Awaitility.await()
                .atMost(4, TimeUnit.SECONDS)
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
}
