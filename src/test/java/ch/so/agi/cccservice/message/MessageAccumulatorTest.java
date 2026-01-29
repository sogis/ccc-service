package ch.so.agi.cccservice.message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.so.agi.cccservice.session.MockWebSocketSession;

class MessageAccumulatorTest {

    private MessageAccumulator accumulator;
    private MockWebSocketSession session;

    @BeforeEach
    void setUp() {
        accumulator = new MessageAccumulator();
        session = new MockWebSocketSession();
    }

    /*
    ┌─────────────────────────────────────────┬──────────────────────────────────────────────────┐
    │                  Test                   │                   Beschreibung                   │
    ├─────────────────────────────────────────┼──────────────────────────────────────────────────┤
    │ completeMessageIsPassedDirectly         │ Vollständige Nachricht wird direkt durchgereicht │
    ├─────────────────────────────────────────┼──────────────────────────────────────────────────┤
    │ fragmentedMessageIsAssembled            │ Fragmente werden korrekt zusammengebaut          │
    ├─────────────────────────────────────────┼──────────────────────────────────────────────────┤
    │ multipleSessionsAreHandledIndependently │ Mehrere Sessions haben separate Buffer           │
    ├─────────────────────────────────────────┼──────────────────────────────────────────────────┤
    │ cleanupRemovesBufferForSession          │ Cleanup entfernt Buffer bei Connection-Close     │
    ├─────────────────────────────────────────┼──────────────────────────────────────────────────┤
    │ cleanupForUnknownSessionDoesNothing     │ Cleanup für unbekannte Session ist sicher        │
    ├─────────────────────────────────────────┼──────────────────────────────────────────────────┤
    │ expiredBufferIsDiscardedOnNextFragment  │ Abgelaufener Buffer wird verworfen               │
    ├─────────────────────────────────────────┼──────────────────────────────────────────────────┤
    │ purgeExpiredRemovesStaleBuffers         │ purgeExpired räumt alte Buffer auf               │
    ├─────────────────────────────────────────┼──────────────────────────────────────────────────┤
    │ purgeExpiredKeepsNonExpiredBuffers      │ Aktive Buffer bleiben erhalten                   │
    ├─────────────────────────────────────────┼──────────────────────────────────────────────────┤
    │ oversizedMessageIsDiscarded             │ Zu grosse Nachricht wird verworfen               │
    ├─────────────────────────────────────────┼──────────────────────────────────────────────────┤
    │ newMessageAfterOversizeStartsFresh      │ Nach Oversize funktioniert neue Nachricht        │
    └─────────────────────────────────────────┴──────────────────────────────────────────────────┘
     */

    @Test
    void completeMessageIsPassedDirectly() {
        AtomicReference<String> result = new AtomicReference<>();

        accumulator.accumulate(session, "complete message", true, result::set);

        assertEquals("complete message", result.get());
        assertEquals(0, accumulator.getActiveBufferCount());
    }

    @Test
    void fragmentedMessageIsAssembled() {
        AtomicReference<String> result = new AtomicReference<>();

        accumulator.accumulate(session, "part1", false, result::set);
        assertNull(result.get(), "Callback should not be called for partial message");
        assertEquals(1, accumulator.getActiveBufferCount());

        accumulator.accumulate(session, "part2", false, result::set);
        assertNull(result.get(), "Callback should not be called for partial message");

        accumulator.accumulate(session, "part3", true, result::set);
        assertEquals("part1part2part3", result.get());
        assertEquals(0, accumulator.getActiveBufferCount());
    }

    @Test
    void multipleSessionsAreHandledIndependently() {
        MockWebSocketSession session1 = new MockWebSocketSession();
        MockWebSocketSession session2 = new MockWebSocketSession();
        List<String> results = new ArrayList<>();

        accumulator.accumulate(session1, "s1-part1", false, results::add);
        accumulator.accumulate(session2, "s2-part1", false, results::add);
        assertEquals(2, accumulator.getActiveBufferCount());

        accumulator.accumulate(session1, "s1-part2", true, results::add);
        assertEquals(1, results.size());
        assertEquals("s1-part1s1-part2", results.get(0));

        accumulator.accumulate(session2, "s2-part2", true, results::add);
        assertEquals(2, results.size());
        assertEquals("s2-part1s2-part2", results.get(1));

        assertEquals(0, accumulator.getActiveBufferCount());
    }

    @Test
    void cleanupRemovesBufferForSession() {
        accumulator.accumulate(session, "partial", false, msg -> {
        });
        assertEquals(1, accumulator.getActiveBufferCount());

        accumulator.cleanup(session);
        assertEquals(0, accumulator.getActiveBufferCount());
    }

    @Test
    void cleanupForUnknownSessionDoesNothing() {
        MockWebSocketSession unknownSession = new MockWebSocketSession();

        assertDoesNotThrow(() -> accumulator.cleanup(unknownSession));
    }

    @Test
    void expiredBufferIsDiscardedOnNextFragment() throws InterruptedException {
        // Akkumulator mit 1 Sekunde Timeout
        MessageAccumulator shortTimeoutAccumulator = new MessageAccumulator(1);
        AtomicReference<String> result = new AtomicReference<>();

        shortTimeoutAccumulator.accumulate(session, "old-part", false, result::set);
        assertEquals(1, shortTimeoutAccumulator.getActiveBufferCount());

        // Warten bis Timeout abläuft
        Thread.sleep(1100);

        // Neues Fragment - alter Buffer sollte verworfen werden
        shortTimeoutAccumulator.accumulate(session, "new-part1", false, result::set);
        assertNull(result.get());

        shortTimeoutAccumulator.accumulate(session, "new-part2", true, result::set);
        assertEquals("new-part1new-part2", result.get(), "Should only contain new fragments");
    }

    @Test
    void purgeExpiredRemovesStaleBuffers() throws InterruptedException {
        MessageAccumulator shortTimeoutAccumulator = new MessageAccumulator(1);

        shortTimeoutAccumulator.accumulate(session, "partial", false, msg -> {
        });
        assertEquals(1, shortTimeoutAccumulator.getActiveBufferCount());

        Thread.sleep(1100);

        int purged = shortTimeoutAccumulator.purgeExpired();
        assertEquals(1, purged);
        assertEquals(0, shortTimeoutAccumulator.getActiveBufferCount());
    }

    @Test
    void purgeExpiredKeepsNonExpiredBuffers() {
        accumulator.accumulate(session, "partial", false, msg -> {
        });

        int purged = accumulator.purgeExpired();
        assertEquals(0, purged);
        assertEquals(1, accumulator.getActiveBufferCount());
    }

    @Test
    void oversizedMessageIsDiscarded() {
        // Akkumulator mit kleinerem Limit für schnelleren Test
        MessageAccumulator smallLimitAccumulator = new MessageAccumulator(30, 1000);
        AtomicReference<String> result = new AtomicReference<>();

        // Fragment knapp unter dem Limit
        String chunk = "x".repeat(800);
        smallLimitAccumulator.accumulate(session, chunk, false, result::set);
        assertEquals(1, smallLimitAccumulator.getActiveBufferCount());

        // Nächstes Fragment würde Limit überschreiten
        String overflowChunk = "y".repeat(300); // 800 + 300 = 1100 > 1000
        smallLimitAccumulator.accumulate(session, overflowChunk, false, result::set);

        // Buffer sollte verworfen worden sein
        assertEquals(0, smallLimitAccumulator.getActiveBufferCount());
        assertNull(result.get());
    }

    @Test
    void newMessageAfterOversizeStartsFresh() {
        MessageAccumulator smallLimitAccumulator = new MessageAccumulator(30, 1000);
        AtomicReference<String> result = new AtomicReference<>();

        // Überschreite das Limit
        String chunk = "x".repeat(800);
        String overflowChunk = "y".repeat(300);
        smallLimitAccumulator.accumulate(session, chunk, false, result::set);
        smallLimitAccumulator.accumulate(session, overflowChunk, false, result::set);
        assertEquals(0, smallLimitAccumulator.getActiveBufferCount());

        // Neue Nachricht sollte normal funktionieren
        smallLimitAccumulator.accumulate(session, "fresh", true, result::set);
        assertEquals("fresh", result.get());
    }
}
