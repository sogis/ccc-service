package ch.so.agi.cccservice.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ConnectionRateLimiterTest {

    // --- Reconnect Limiter Tests ---

    @Nested
    class ReconnectLimiterTests {

        private ConnectionRateLimiter rateLimiter;

        @BeforeEach
        void setUp() {
            rateLimiter = ConnectionRateLimiter.getReconnectLimiter();
        }

        private void resetIp(String ip) {
            rateLimiter.recordSuccess(ip);
        }

        @Test
        void firstAttempt_isAllowed() {
            String ip = "10.0.0.1";
            resetIp(ip);

            assertTrue(rateLimiter.isAllowed(ip));
        }

        @Test
        void twoFailedAttempts_stillAllowed() {
            String ip = "10.0.0.2";
            resetIp(ip);

            rateLimiter.recordFailedAttempt(ip);
            rateLimiter.recordFailedAttempt(ip);

            assertTrue(rateLimiter.isAllowed(ip));
        }

        @Test
        void threeFailedAttempts_blocked() {
            String ip = "10.0.0.3";
            resetIp(ip);

            rateLimiter.recordFailedAttempt(ip);
            rateLimiter.recordFailedAttempt(ip);
            rateLimiter.recordFailedAttempt(ip);

            assertFalse(rateLimiter.isAllowed(ip));
        }

        @Test
        void successResetsFailureCount() {
            String ip = "10.0.0.4";
            resetIp(ip);

            rateLimiter.recordFailedAttempt(ip);
            rateLimiter.recordFailedAttempt(ip);
            rateLimiter.recordSuccess(ip);

            // After success, should be allowed again
            assertTrue(rateLimiter.isAllowed(ip));

            // Even after 2 more failures, still allowed (reset worked)
            rateLimiter.recordFailedAttempt(ip);
            rateLimiter.recordFailedAttempt(ip);
            assertTrue(rateLimiter.isAllowed(ip));
        }

        @Test
        void differentIps_trackedSeparately() {
            String ip1 = "10.0.0.5";
            String ip2 = "10.0.0.6";
            resetIp(ip1);
            resetIp(ip2);

            // Block ip1
            rateLimiter.recordFailedAttempt(ip1);
            rateLimiter.recordFailedAttempt(ip1);
            rateLimiter.recordFailedAttempt(ip1);

            assertFalse(rateLimiter.isAllowed(ip1));
            assertTrue(rateLimiter.isAllowed(ip2));
        }

        @Test
        void nullOrBlankIp_alwaysAllowed() {
            assertTrue(rateLimiter.isAllowed(null));
            assertTrue(rateLimiter.isAllowed(""));
            assertTrue(rateLimiter.isAllowed("   "));
        }

        @Test
        void nullIp_failureNotRecorded() {
            assertDoesNotThrow(() -> rateLimiter.recordFailedAttempt(null));
            assertDoesNotThrow(() -> rateLimiter.recordFailedAttempt(""));
        }

        @Test
        void nullIp_successNotRecorded() {
            assertDoesNotThrow(() -> rateLimiter.recordSuccess(null));
        }

        @Test
        void fourFailedAttempts_stillBlocked() {
            String ip = "10.0.0.7";
            resetIp(ip);

            for (int i = 0; i < 4; i++) {
                rateLimiter.recordFailedAttempt(ip);
            }

            assertFalse(rateLimiter.isAllowed(ip));
        }

        @Test
        void sixFailedAttempts_stillBlocked() {
            String ip = "10.0.0.8";
            resetIp(ip);

            for (int i = 0; i < 6; i++) {
                rateLimiter.recordFailedAttempt(ip);
            }

            assertFalse(rateLimiter.isAllowed(ip));
        }
    }

    // --- Connect Limiter Tests ---

    @Nested
    class ConnectLimiterTests {

        private ConnectionRateLimiter rateLimiter;

        @BeforeEach
        void setUp() {
            rateLimiter = ConnectionRateLimiter.getConnectLimiter();
        }

        private void resetIp(String ip) {
            rateLimiter.recordSuccess(ip);
        }

        @Test
        void firstAttempt_isAllowed() {
            String ip = "10.1.0.1";
            resetIp(ip);

            assertTrue(rateLimiter.isAllowed(ip));
        }

        @Test
        void threeFailedAttempts_blocked() {
            String ip = "10.1.0.2";
            resetIp(ip);

            rateLimiter.recordFailedAttempt(ip);
            rateLimiter.recordFailedAttempt(ip);
            rateLimiter.recordFailedAttempt(ip);

            assertFalse(rateLimiter.isAllowed(ip));
        }

        @Test
        void successResetsFailureCount() {
            String ip = "10.1.0.3";
            resetIp(ip);

            rateLimiter.recordFailedAttempt(ip);
            rateLimiter.recordFailedAttempt(ip);
            rateLimiter.recordSuccess(ip);

            assertTrue(rateLimiter.isAllowed(ip));
        }
    }

    // --- Limiter Independence Tests ---

    @Nested
    class LimiterIndependenceTests {

        @Test
        void reconnectAndConnectLimiters_areDifferentInstances() {
            ConnectionRateLimiter reconnect = ConnectionRateLimiter.getReconnectLimiter();
            ConnectionRateLimiter connect = ConnectionRateLimiter.getConnectLimiter();

            assertNotSame(reconnect, connect);
        }

        @Test
        void reconnectAndConnectLimiters_trackSeparately() {
            ConnectionRateLimiter reconnectLimiter = ConnectionRateLimiter.getReconnectLimiter();
            ConnectionRateLimiter connectLimiter = ConnectionRateLimiter.getConnectLimiter();

            String ip = "10.2.0.1";
            reconnectLimiter.recordSuccess(ip);
            connectLimiter.recordSuccess(ip);

            // Block in reconnect limiter
            reconnectLimiter.recordFailedAttempt(ip);
            reconnectLimiter.recordFailedAttempt(ip);
            reconnectLimiter.recordFailedAttempt(ip);

            // Reconnect should be blocked, Connect should be allowed
            assertFalse(reconnectLimiter.isAllowed(ip));
            assertTrue(connectLimiter.isAllowed(ip));
        }

        @Test
        void sameInstance_returnedOnMultipleCalls() {
            ConnectionRateLimiter first = ConnectionRateLimiter.getReconnectLimiter();
            ConnectionRateLimiter second = ConnectionRateLimiter.getReconnectLimiter();

            assertSame(first, second);
        }
    }
}
