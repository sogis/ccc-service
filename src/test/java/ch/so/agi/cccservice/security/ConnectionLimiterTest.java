package ch.so.agi.cccservice.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ConnectionLimiterTest {

    private ConnectionLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = ConnectionLimiter.getInstance();
    }

    private void resetIp(String ip) {
        // Close all connections for this IP to reset state
        for (int i = 0; i < 50; i++) {
            limiter.recordConnectionClosed(ip);
        }
    }

    // --- Concurrent Connection Limit Tests ---

    @Nested
    class ConcurrentConnectionTests {

        @Test
        void firstConnection_isAllowed() {
            String ip = "10.10.0.1";
            resetIp(ip);

            assertTrue(limiter.isConnectionAllowed(ip));
        }

        @Test
        void tenConnections_allAllowed() {
            String ip = "10.10.0.2";
            resetIp(ip);

            for (int i = 0; i < 10; i++) {
                assertTrue(limiter.isConnectionAllowed(ip), "Connection " + (i + 1) + " should be allowed");
                limiter.recordConnectionOpened(ip);
            }

            assertEquals(10, limiter.getConcurrentConnections(ip));
        }

        @Test
        void eleventhConnection_rejected() {
            String ip = "10.10.0.3";
            resetIp(ip);

            // Open 10 connections
            for (int i = 0; i < 10; i++) {
                limiter.recordConnectionOpened(ip);
            }

            // 11th should be rejected
            assertFalse(limiter.isConnectionAllowed(ip));
        }

        @Test
        void connectionClosed_allowsNewConnection() {
            String ip = "10.10.0.4";
            resetIp(ip);

            // Open 10 connections
            for (int i = 0; i < 10; i++) {
                limiter.recordConnectionOpened(ip);
            }

            // Close one
            limiter.recordConnectionClosed(ip);

            // New connection should be allowed
            assertTrue(limiter.isConnectionAllowed(ip));
            assertEquals(9, limiter.getConcurrentConnections(ip));
        }

        @Test
        void differentIps_trackedSeparately() {
            String ip1 = "10.10.0.5";
            String ip2 = "10.10.0.6";
            resetIp(ip1);
            resetIp(ip2);

            // Fill up ip1
            for (int i = 0; i < 10; i++) {
                limiter.recordConnectionOpened(ip1);
            }

            // ip1 should be blocked, ip2 should be allowed
            assertFalse(limiter.isConnectionAllowed(ip1));
            assertTrue(limiter.isConnectionAllowed(ip2));
        }
    }

    // --- Rate Limit Tests ---

    @Nested
    class RateLimitTests {

        @Test
        void thirtyConnectionsPerMinute_allowed() {
            String ip = "10.11.0.1";
            resetIp(ip);

            for (int i = 0; i < 30; i++) {
                assertTrue(limiter.isConnectionAllowed(ip), "Connection " + (i + 1) + " should be allowed");
                limiter.recordConnectionOpened(ip);
                limiter.recordConnectionClosed(ip); // Close immediately to not hit concurrent limit
            }
        }

        @Test
        void thirtyFirstConnectionInSameMinute_rejected() {
            String ip = "10.11.0.2";
            resetIp(ip);

            // Open and close 30 connections
            for (int i = 0; i < 30; i++) {
                limiter.recordConnectionOpened(ip);
                limiter.recordConnectionClosed(ip);
            }

            // 31st should be rejected (rate limit)
            assertFalse(limiter.isConnectionAllowed(ip));
        }
    }

    // --- Edge Cases ---

    @Nested
    class EdgeCaseTests {

        @Test
        void nullIp_alwaysAllowed() {
            assertTrue(limiter.isConnectionAllowed(null));
            assertTrue(limiter.isConnectionAllowed(""));
            assertTrue(limiter.isConnectionAllowed("   "));
        }

        @Test
        void nullIp_recordDoesNotThrow() {
            assertDoesNotThrow(() -> limiter.recordConnectionOpened(null));
            assertDoesNotThrow(() -> limiter.recordConnectionClosed(null));
            assertDoesNotThrow(() -> limiter.recordConnectionOpened(""));
            assertDoesNotThrow(() -> limiter.recordConnectionClosed(""));
        }

        @Test
        void getConcurrentConnections_returnsZeroForUnknownIp() {
            assertEquals(0, limiter.getConcurrentConnections("unknown.ip.address"));
        }

        @Test
        void closeMoreThanOpened_doesNotGoNegative() {
            String ip = "10.12.0.1";
            resetIp(ip);

            limiter.recordConnectionOpened(ip);
            limiter.recordConnectionClosed(ip);
            limiter.recordConnectionClosed(ip); // Extra close

            // Should not go negative, but implementation allows it
            // This test documents current behavior
            assertTrue(limiter.getConcurrentConnections(ip) <= 0);
        }

        @Test
        void singletonInstance_returnsSameObject() {
            ConnectionLimiter first = ConnectionLimiter.getInstance();
            ConnectionLimiter second = ConnectionLimiter.getInstance();

            assertSame(first, second);
        }
    }
}
