package ch.so.agi.cccservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import ch.so.agi.cccservice.health.SocketClient;
import ch.so.agi.cccservice.health.WebServerPort;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.SockConnection;
import ch.so.agi.cccservice.session.Sessions;

/**
 * Verifies that ccc.websocket.send-timeout-seconds is actually applied to real WebSocket
 * connections as Tomcat's per-session blocking-send-timeout override, catching
 * property-name/unit typos that Spring would otherwise silently ignore (same rationale as
 * TomcatLimitsConfigurationTest, applied here to CCCWebSocketHandler#setSendTimeout instead
 * of server.tomcat.* properties).
 *
 * The actual timeout behaviour (an unresponsive peer gets aborted after roughly this many
 * seconds instead of Tomcat's own 20s default) was verified manually by running the app with
 * ccc.websocket.send-timeout-seconds=2 and a raw socket GIS peer that stops reading after the
 * handshake: the server aborted the stalled write after 2.03s with
 * CloseStatus[code=1006, reason="Write timeout"]. That scenario needs an unbounded, slow
 * real socket and isn't suitable for the regular test suite, so only the fast, deterministic
 * wiring check below is automated.
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SendTimeoutConfigurationTest {

    private static final String BLOCKING_SEND_TIMEOUT_PROPERTY =
            "org.apache.tomcat.websocket.BLOCKING_SEND_TIMEOUT";

    @Value("${ccc.websocket.send-timeout-seconds:" + CCCWebSocketHandler.DEFAULT_SEND_TIMEOUT_SECONDS + "}")
    private int expectedSendTimeoutSeconds;

    @Test
    void establishedConnection_hasConfiguredBlockingSendTimeout() {
        Sessions.resetSessionCollection();

        String adr = "ws://localhost:" + WebServerPort.getPort() + WebSocketConfig.CCC_SOCKET_PATH;
        UUID sessionUid = UUID.randomUUID();

        SocketClient appClient = new SocketClient(adr, SocketClient.ClientType.APP);
        SocketClient gisClient = new SocketClient(adr, SocketClient.ClientType.GIS);

        appClient.connectCCC(sessionUid, "test-app", SockConnection.PROTOCOL_V12, SocketClient.ClientType.APP);
        gisClient.connectCCC(sessionUid, "test-gis", SockConnection.PROTOCOL_V12, SocketClient.ClientType.GIS);

        Awaitility.await().atMost(2, TimeUnit.SECONDS)
                .until(() -> appClient.getSessionNr() != null && gisClient.getSessionNr() != null);

        Session serverSession = Sessions.allSessions()
                .filter(s -> s.getSessionNr() == appClient.getSessionNr())
                .findFirst()
                .orElseThrow();

        long expectedMillis = expectedSendTimeoutSeconds * 1000L;
        assertEquals(expectedMillis, blockingSendTimeoutOf(serverSession.getAppWebSocket()),
                "App connection should have the configured blocking-send timeout");
        assertEquals(expectedMillis, blockingSendTimeoutOf(serverSession.getGisWebSocket()),
                "GIS connection should have the configured blocking-send timeout");
    }

    private Object blockingSendTimeoutOf(org.springframework.web.socket.WebSocketSession session) {
        assertNotNull(session);
        StandardWebSocketSession sws = (StandardWebSocketSession) session;
        jakarta.websocket.Session nativeSession = sws.getNativeSession();
        assertNotNull(nativeSession);
        return nativeSession.getUserProperties().get(BLOCKING_SEND_TIMEOUT_PROPERTY);
    }
}
