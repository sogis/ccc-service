package ch.so.agi.cccservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Asserts that the Tomcat resource limits configured in application.properties
 * are actually bound to Spring Boot's ServerProperties. Catches property-name
 * typos which Spring would otherwise silently ignore, leaving Tomcat on its
 * (much higher) defaults (8192 max-connections, 200 threads, 100 accept-count).
 *
 * Expected values are injected from the property file so the test does not have
 * to be updated in lockstep with limit changes. The typo-detection still holds:
 * if a key in application.properties does not match the official Spring Boot
 * Tomcat key, the @Value-injected expectation will differ from the value
 * Spring Boot bound into ServerProperties, and the assertion fails.
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TomcatLimitsConfigurationTest {

    @Autowired
    private ServerProperties serverProperties;

    @Value("${server.tomcat.max-connections}")
    private int expectedMaxConnections;

    @Value("${server.tomcat.threads.max}")
    private int expectedThreadsMax;

    @Value("${server.tomcat.accept-count}")
    private int expectedAcceptCount;

    @Value("${server.tomcat.connection-timeout}")
    private Duration expectedConnectionTimeout;

    @Test
    void maxConnections_matchesApplicationProperties() {
        assertEquals(expectedMaxConnections, serverProperties.getTomcat().getMaxConnections());
    }

    @Test
    void threadsMax_matchesApplicationProperties() {
        assertEquals(expectedThreadsMax, serverProperties.getTomcat().getThreads().getMax());
    }

    @Test
    void acceptCount_matchesApplicationProperties() {
        assertEquals(expectedAcceptCount, serverProperties.getTomcat().getAcceptCount());
    }

    @Test
    void connectionTimeout_matchesApplicationProperties() {
        assertEquals(expectedConnectionTimeout, serverProperties.getTomcat().getConnectionTimeout());
    }
}
