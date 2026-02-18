package ch.so.agi.cccservice.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Deep health check that verifies WebSocket functionality through reconnect and message exchange.
 * This is more expensive than the basic readiness probe and should be called less frequently.
 *
 * Use /actuator/health/websocket for periodic deep checks (every 60s).
 */
@Component("websocket")
public class WebSocketHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHealthIndicator.class);

    private TestClient client;

    @Override
    public synchronized Health health() {
        try {
            if(client == null)
                client = new TestClient();

            log.debug("Session {}: Verifying WebSocket health through reconnect and payload message.", client.getSessionNr());
            client.reconnectAndSend();

            return Health.up()
                .withDetail("session", client.getSessionNr())
                .withDetail("status", "WebSocket operational")
                .build();
        } catch (Exception e) {
            log.error("WebSocket health check failed", e);
            client = null;
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("status", "WebSocket check failed")
                .build();
        }
    }
}
