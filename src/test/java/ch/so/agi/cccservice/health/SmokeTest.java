package ch.so.agi.cccservice.health;

import static ch.so.agi.cccservice.health.SocketClient.ClientType.APP;
import static ch.so.agi.cccservice.health.SocketClient.ClientType.GIS;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("smoke")
class SmokeTest {

    private static final String PROPERTY_KEY = "ccc.smoke.url";

    @Test
    void connectAndReconnect() throws InterruptedException {
        String url = resolveUrl();

        // 1. Both clients connect (new session)
        SocketClient gis = new SocketClient(url, GIS);
        SocketClient app = new SocketClient(url, APP);
        UUID session = UUID.randomUUID();
        gis.connectCCC(session, "smoke-gis", "1.2", GIS);
        app.connectCCC(session, "smoke-app", "1.2", APP);

        // 2. GIS reconnect + send message
        gis.reconnectCCC();
        gis.sendMinimalCCCMessage();

        // 3. APP reconnect + send message
        app.reconnectCCC();
        app.sendMinimalCCCMessage();

        // 4. Cleanup — wait for server to finish forwarding the last message before closing
        Thread.sleep(500);
        gis.closeWebSocket();
        app.closeWebSocket();
    }

    @Test
    void rapidReconnects_multipleRounds() throws InterruptedException {
        String url = resolveUrl();

        // 1. Both clients connect (new session)
        SocketClient gis = new SocketClient(url, GIS);
        SocketClient app = new SocketClient(url, APP);
        UUID session = UUID.randomUUID();
        gis.connectCCC(session, "smoke-gis", "1.2", GIS);
        app.connectCCC(session, "smoke-app", "1.2", APP);

        // 2. Multiple reconnect rounds to test stability
        for (int i = 0; i < 5; i++) {
            gis.reconnectCCC();
            app.reconnectCCC();
            gis.sendMinimalCCCMessage();
            app.sendMinimalCCCMessage();
            Thread.sleep(100);
        }

        // 3. Cleanup
        Thread.sleep(500);
        gis.closeWebSocket();
        app.closeWebSocket();
    }

    @Test
    void connectAndDisconnect() throws InterruptedException {
        String url = resolveUrl();

        // 1. Both clients connect (new session)
        SocketClient gis = new SocketClient(url, GIS);
        SocketClient app = new SocketClient(url, APP);
        UUID session = UUID.randomUUID();
        gis.connectCCC(session, "smoke-gis", "1.2", GIS);
        app.connectCCC(session, "smoke-app", "1.2", APP);

        // 2. APP sends disconnect — server should close both connections
        Thread.sleep(500);
        app.disconnectCCC();

        // 3. Verify both connections were closed by the server
        Thread.sleep(500);
        assert !app.webSocketIsOpen() : "App connection should be closed after disconnect";
        assert !gis.webSocketIsOpen() : "Gis connection should be closed after disconnect";
    }

    @Test
    void connectAndDisconnectFromGis() throws InterruptedException {
        String url = resolveUrl();

        // 1. Both clients connect (new session)
        SocketClient gis = new SocketClient(url, GIS);
        SocketClient app = new SocketClient(url, APP);
        UUID session = UUID.randomUUID();
        gis.connectCCC(session, "smoke-gis", "1.2", GIS);
        app.connectCCC(session, "smoke-app", "1.2", APP);

        // 2. GIS sends disconnect — server should close both connections
        Thread.sleep(500);
        gis.disconnectCCC();

        // 3. Verify both connections were closed by the server
        Thread.sleep(500);
        assert !gis.webSocketIsOpen() : "Gis connection should be closed after disconnect";
        assert !app.webSocketIsOpen() : "App connection should be closed after disconnect";
    }

    private static String resolveUrl() {
        String sysProp = System.getProperty(PROPERTY_KEY);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }

        Properties props = new Properties();
        try (InputStream in = SmokeTest.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }

        String value = props.getProperty(PROPERTY_KEY);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "No smoke test URL configured. Set '" + PROPERTY_KEY
                            + "' in application.properties or pass -D" + PROPERTY_KEY + "=...");
        }
        return value;
    }
}
