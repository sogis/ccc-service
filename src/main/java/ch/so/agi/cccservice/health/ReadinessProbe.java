package ch.so.agi.cccservice.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("cccReadiness")
public class ReadinessProbe implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(ReadinessProbe.class);

    private TestClient client;

    @Override
    public synchronized Health health() {
        try {
            if(client == null)
                client = new TestClient();

            log.info("Session {}: Verifying service health through reconnect followed by payload message.", client.getSessionNr());
            client.reconnectAndSend();
            return Health.up().withDetail("readiness", "ccc-service is ready").build();
        } catch (Exception e) {
            log.error("Health check threw exception.", e);
            client = null;
            return Health.down().withDetail("readiness", "readiness check failed").build();
        }
    }
}

