package ch.so.agi.cccservice.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("cccLiveness")
public class LivenessProbe implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(LivenessProbe.class);

    private TestClient client;

    @Override
    public synchronized Health health() {
        try {
            if(client == null)
                client = new TestClient();

            log.info("Session {}: Verifying service health through reconnect followed by payload message.", client.getSessionNr());
            client.reconnectAndSend();
            return Health.up().withDetail("liveness", "ccc-service is alive").build();
        } catch (Exception e) {
            log.error("Health check threw exception.", e);
            return Health.down().withDetail("liveness", "liveness check failed").build();
        }
    }
}

