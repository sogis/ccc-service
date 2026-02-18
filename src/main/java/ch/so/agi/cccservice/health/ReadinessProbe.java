package ch.so.agi.cccservice.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Lightweight readiness probe for Kubernetes/OpenShift rolling updates.
 * Simply returns UP to indicate the application is ready to serve traffic.
 *
 * Use /actuator/health/readiness for fast rolling update checks (every 5s).
 */
@Component("cccReadiness")
public class ReadinessProbe implements HealthIndicator {

    @Override
    public Health health() {
        // Lightweight check: if this code runs, Spring Boot is ready
        return Health.up()
            .withDetail("status", "ready")
            .build();
    }
}

