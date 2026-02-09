package ch.so.agi.cccservice.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Limits the number of WebSocket connections per IP address to prevent DoS attacks.
 *
 * Enforces two limits:
 * - Maximum concurrent connections per IP
 * - Maximum new connections per time window (rate limiting)
 */
public class ConnectionLimiter {

    private static final Logger log = LoggerFactory.getLogger(ConnectionLimiter.class);
    private static final ConnectionLimiter INSTANCE = new ConnectionLimiter();

    public static ConnectionLimiter getInstance() {
        return INSTANCE;
    }

    private static final int MAX_CONCURRENT_CONNECTIONS_PER_IP = 10;
    private static final int MAX_CONNECTIONS_PER_MINUTE = 30;
    private static final Duration RATE_WINDOW = Duration.ofMinutes(1);
    private static final Duration CLEANUP_INTERVAL = Duration.ofMinutes(5);

    private final Map<String, ConnectionRecord> connectionsByIp = new ConcurrentHashMap<>();
    private volatile Instant lastCleanup = Instant.now();

    private ConnectionLimiter() {}

    /**
     * Checks if a new connection is allowed for the given IP address.
     *
     * @param ipAddress the client's IP address
     * @return true if allowed, false if limit exceeded
     */
    public boolean isConnectionAllowed(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return true;
        }

        cleanupIfNeeded();

        ConnectionRecord record = connectionsByIp.computeIfAbsent(ipAddress, ip -> new ConnectionRecord());

        // Check concurrent connection limit
        if (record.getConcurrentCount() >= MAX_CONCURRENT_CONNECTIONS_PER_IP) {
            log.warn("Connection from {} rejected: max concurrent connections ({}) exceeded",
                    ipAddress, MAX_CONCURRENT_CONNECTIONS_PER_IP);
            return false;
        }

        // Check rate limit
        if (record.getConnectionsInWindow() >= MAX_CONNECTIONS_PER_MINUTE) {
            log.warn("Connection from {} rejected: rate limit ({}/min) exceeded",
                    ipAddress, MAX_CONNECTIONS_PER_MINUTE);
            return false;
        }

        return true;
    }

    /**
     * Records a new connection being opened.
     *
     * @param ipAddress the client's IP address
     */
    public void recordConnectionOpened(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return;
        }

        connectionsByIp.computeIfAbsent(ipAddress, ip -> new ConnectionRecord())
                .connectionOpened();
    }

    /**
     * Records a connection being closed.
     *
     * @param ipAddress the client's IP address
     */
    public void recordConnectionClosed(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return;
        }

        ConnectionRecord record = connectionsByIp.get(ipAddress);
        if (record != null) {
            record.connectionClosed();
        }
    }

    /**
     * Returns the current number of concurrent connections for an IP.
     * Useful for monitoring/testing.
     */
    public int getConcurrentConnections(String ipAddress) {
        ConnectionRecord record = connectionsByIp.get(ipAddress);
        return record != null ? record.getConcurrentCount() : 0;
    }

    /**
     * Resets all connection tracking. For testing purposes only.
     */
    public void reset() {
        connectionsByIp.clear();
    }

    private void cleanupIfNeeded() {
        if (Instant.now().isAfter(lastCleanup.plus(CLEANUP_INTERVAL))) {
            synchronized (this) {
                if (Instant.now().isAfter(lastCleanup.plus(CLEANUP_INTERVAL))) {
                    connectionsByIp.entrySet().removeIf(entry ->
                            entry.getValue().isInactive());
                    lastCleanup = Instant.now();
                }
            }
        }
    }

    /**
     * Tracks connection state per IP address.
     */
    private static class ConnectionRecord {
        private final AtomicInteger concurrentConnections = new AtomicInteger(0);
        private final AtomicInteger connectionsInWindow = new AtomicInteger(0);
        private volatile Instant windowStart = Instant.now();

        void connectionOpened() {
            concurrentConnections.incrementAndGet();

            // Reset window if expired
            if (Instant.now().isAfter(windowStart.plus(RATE_WINDOW))) {
                synchronized (this) {
                    if (Instant.now().isAfter(windowStart.plus(RATE_WINDOW))) {
                        connectionsInWindow.set(0);
                        windowStart = Instant.now();
                    }
                }
            }
            connectionsInWindow.incrementAndGet();
        }

        void connectionClosed() {
            concurrentConnections.decrementAndGet();
        }

        int getConcurrentCount() {
            return concurrentConnections.get();
        }

        int getConnectionsInWindow() {
            // Reset window if expired
            if (Instant.now().isAfter(windowStart.plus(RATE_WINDOW))) {
                return 0;
            }
            return connectionsInWindow.get();
        }

        boolean isInactive() {
            return concurrentConnections.get() <= 0
                    && Instant.now().isAfter(windowStart.plus(RATE_WINDOW));
        }
    }
}
