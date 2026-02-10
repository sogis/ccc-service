package ch.so.agi.cccservice.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rate limiter for connection attempts to prevent brute-force and DoS attacks.
 * Uses exponential backoff after failed attempts.
 * Implemented as singleton for access from non-Spring-managed classes.
 *
 * Provides separate limiters for:
 * - Reconnect attempts (brute-force protection)
 * - Connect attempts (DoS protection)
 */
public class ConnectionRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(ConnectionRateLimiter.class);

    // Separate instances for different use cases
    private static final ConnectionRateLimiter RECONNECT_LIMITER = new ConnectionRateLimiter("reconnect");
    private static final ConnectionRateLimiter CONNECT_LIMITER = new ConnectionRateLimiter("connect");

    public static ConnectionRateLimiter getReconnectLimiter() {
        return RECONNECT_LIMITER;
    }

    public static ConnectionRateLimiter getConnectLimiter() {
        return CONNECT_LIMITER;
    }

    private final String limiterType;
    private volatile boolean enabled = false;

    private ConnectionRateLimiter(String limiterType) {
        this.limiterType = limiterType;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("ConnectionRateLimiter [{}] {}", limiterType, enabled ? "enabled" : "disabled");
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static final int MAX_ATTEMPTS_BEFORE_LIMIT = 2;
    private static final Duration CLEANUP_INTERVAL = Duration.ofMinutes(10);

    private final Map<String, AttemptRecord> attemptsByIp = new ConcurrentHashMap<>();
    private volatile Instant lastCleanup = Instant.now();

    /**
     * Checks if an attempt is allowed for the given IP address.
     *
     * @param ipAddress the client's IP address
     * @return true if allowed, false if rate limited
     */
    public boolean isAllowed(String ipAddress) {
        if (!enabled) {
            return true;
        }

        if (ipAddress == null || ipAddress.isBlank()) {
            return true; // Allow if IP unknown (shouldn't happen)
        }

        cleanupIfNeeded();

        AttemptRecord record = attemptsByIp.get(ipAddress);
        if (record == null) {
            return true;
        }

        if (record.isBlocked()) {
            Duration remaining = record.getBlockedUntil();
            log.warn("{} attempt from {} blocked for {} more seconds",
                    limiterType, ipAddress, remaining.toSeconds());
            return false;
        }

        return true;
    }

    /**
     * Records a failed attempt for the given IP address.
     *
     * @param ipAddress the client's IP address
     */
    public void recordFailedAttempt(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return;
        }

        attemptsByIp.compute(ipAddress, (ip, existing) -> {
            if (existing == null) {
                return new AttemptRecord();
            }
            existing.incrementFailures();
            return existing;
        });

        AttemptRecord record = attemptsByIp.get(ipAddress);
        if (record != null && record.getFailureCount() > MAX_ATTEMPTS_BEFORE_LIMIT) {
            log.warn("IP {} has {} failed {} attempts, applying rate limit",
                    ipAddress, record.getFailureCount(), limiterType);
        }
    }

    /**
     * Records a successful attempt, resetting the failure count for the IP.
     *
     * @param ipAddress the client's IP address
     */
    public void recordSuccess(String ipAddress) {
        if (ipAddress != null) {
            attemptsByIp.remove(ipAddress);
        }
    }

    /**
     * Cleans up old records periodically to prevent memory leaks.
     */
    private void cleanupIfNeeded() {
        if (Instant.now().isAfter(lastCleanup.plus(CLEANUP_INTERVAL))) {
            synchronized (this) {
                if (Instant.now().isAfter(lastCleanup.plus(CLEANUP_INTERVAL))) {
                    int before = attemptsByIp.size();
                    attemptsByIp.entrySet().removeIf(entry ->
                            entry.getValue().isExpired());
                    int removed = before - attemptsByIp.size();
                    if (removed > 0) {
                        log.debug("Cleaned up {} expired rate limit records", removed);
                    }
                    lastCleanup = Instant.now();
                }
            }
        }
    }

    /**
     * Internal record tracking failed attempts per IP.
     */
    private static class AttemptRecord {
        private int failureCount = 1;
        private Instant lastAttempt = Instant.now();
        private Instant blockedUntil = null;

        void incrementFailures() {
            failureCount++;
            lastAttempt = Instant.now();

            // Calculate block duration based on failure count (exponential backoff)
            if (failureCount > MAX_ATTEMPTS_BEFORE_LIMIT) {
                Duration blockDuration = calculateBlockDuration(failureCount);
                blockedUntil = Instant.now().plus(blockDuration);
            }
        }

        private Duration calculateBlockDuration(int failures) {
            return switch (failures) {
                case 3 -> Duration.ofSeconds(5);
                case 4 -> Duration.ofSeconds(15);
                case 5 -> Duration.ofSeconds(60);
                default -> Duration.ofMinutes(5); // 6+ failures
            };
        }

        boolean isBlocked() {
            return blockedUntil != null && Instant.now().isBefore(blockedUntil);
        }

        Duration getBlockedUntil() {
            if (blockedUntil == null) {
                return Duration.ZERO;
            }
            Duration remaining = Duration.between(Instant.now(), blockedUntil);
            return remaining.isNegative() ? Duration.ZERO : remaining;
        }

        int getFailureCount() {
            return failureCount;
        }

        boolean isExpired() {
            // Expire records after 30 minutes of inactivity
            return Instant.now().isAfter(lastAttempt.plus(Duration.ofMinutes(30)));
        }
    }
}
