package ch.so.agi.cccservice.health;

import ch.so.agi.cccservice.WebSocketConfig;
import ch.so.agi.cccservice.session.SockConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.UUID;

//@Component("liveness")
public class LivenessProbe implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(LivenessProbe.class);

    private SocketClient gisClient;
    private SocketClient appClient;

    public LivenessProbe(){
        connectClients();
    }

    //@Override
    public Health health() {
        try {
            reconnectAndSend();
            return Health.up().withDetail("liveness", "ccc-service is alive").build();
        } catch (Exception e) {
            log.error("Health check threw exception.", e);
            return Health.down().withDetail("liveness", "liveness check failed").build();
        }
    }

    private void connectClients(){
        String adr = "ws://localhost:" + WebServerPort.getPort() + WebSocketConfig.CCC_SOCKET_PATH;

        this.gisClient = new SocketClient(adr, SocketClient.ClientType.GIS);
        this.appClient = new SocketClient(adr, SocketClient.ClientType.APP);

        UUID sesUid = UUID.randomUUID();

        gisClient.connectCCC(sesUid, "probe-gis", SockConnection.PROTOCOL_V2, SocketClient.ClientType.GIS);
        appClient.connectCCC(sesUid, "probe-app", SockConnection.PROTOCOL_V2, SocketClient.ClientType.APP);
    }

    private void reconnectAndSend() {
        gisClient.reconnectCCC();
        gisClient.sendMinimalCCCMessage();

        appClient.reconnectCCC();
        appClient.sendMinimalCCCMessage();
    }
}

