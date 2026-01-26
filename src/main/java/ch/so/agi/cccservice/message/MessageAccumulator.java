package ch.so.agi.cccservice.message;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * Akkumuliert fragmentierte WebSocket-Nachrichten zu vollständigen Nachrichten.
 *
 * Bietet Schutz vor Memory-Exhaustion durch:
 * - Maximale Nachrichtengrösse
 * - Timeout für unvollständige Nachrichten
 * - Cleanup bei Connection-Close
 */
@Component
public class MessageAccumulator {

    private static final Logger log = LoggerFactory.getLogger(MessageAccumulator.class);
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int MAX_ACCUMULATED_SIZE = 1024 * 1024; // 1 MB

    private final Map<WebSocketSession, AccumulatorState> buffers = new ConcurrentHashMap<>();
    private final int timeoutSeconds;

    public MessageAccumulator() {
        this(DEFAULT_TIMEOUT_SECONDS);
    }

    public MessageAccumulator(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Akkumuliert eine Teilnachricht oder gibt die vollständige Nachricht zurück.
     *
     * @param session    Die WebSocket-Session
     * @param payload    Der Nachrichteninhalt
     * @param isLast     true wenn dies das letzte Fragment ist
     * @param onComplete Callback für vollständige Nachrichten
     */
    public void accumulate(WebSocketSession session, String payload, boolean isLast,
                           Consumer<String> onComplete) {

        AccumulatorState state = buffers.get(session);

        // Prüfen ob bestehender Buffer abgelaufen oder zu gross ist
        if (state != null) {
            if (state.isExpired(timeoutSeconds)) {
                log.warn("Session {}: Partial message timeout, discarding {} bytes",
                        session.getId(), state.size());
                buffers.remove(session);
                state = null;
            } else if (state.size() + payload.length() > MAX_ACCUMULATED_SIZE) {
                log.error("Session {}: Accumulated message exceeds max size of {} bytes, discarding",
                        session.getId(), MAX_ACCUMULATED_SIZE);
                buffers.remove(session);
                return;
            }
        }

        // Kein Fragment-Modus: Nachricht ist komplett
        if (state == null && isLast) {
            onComplete.accept(payload);
            return;
        }

        // Neuen Buffer starten falls nötig
        if (state == null) {
            state = new AccumulatorState();
            buffers.put(session, state);
        }

        state.append(payload);

        // Letztes Fragment: Zusammenbauen und Callback aufrufen
        if (isLast) {
            String fullMessage = buffers.remove(session).getContent();
            log.debug("Session {}: Assembled message from {} fragments, {} bytes total",
                    session.getId(), state.getFragmentCount(), fullMessage.length());
            onComplete.accept(fullMessage);
        }
    }

    /**
     * Räumt den Buffer für eine geschlossene Session auf.
     */
    public void cleanup(WebSocketSession session) {
        AccumulatorState removed = buffers.remove(session);
        if (removed != null) {
            log.debug("Session {}: Cleaned up incomplete message buffer ({} bytes)",
                    session.getId(), removed.size());
        }
    }

    /**
     * Entfernt alle abgelaufenen Buffer (für periodisches Aufräumen).
     *
     * @return Anzahl der entfernten Buffer
     */
    public int purgeExpired() {
        int[] purgedCount = {0};
        buffers.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired(timeoutSeconds)) {
                log.warn("Purging expired partial message for session {} ({} bytes)",
                        entry.getKey().getId(), entry.getValue().size());
                purgedCount[0]++;
                return true;
            }
            return false;
        });
        return purgedCount[0];
    }

    /**
     * Gibt die Anzahl der aktiven Buffer zurück (für Monitoring/Tests).
     */
    public int getActiveBufferCount() {
        return buffers.size();
    }

    // Innere Klasse für den Akkumulator-Zustand
    private static class AccumulatorState {
        private final StringBuilder buffer = new StringBuilder();
        private final Instant startTime = Instant.now();
        private int fragmentCount = 0;

        void append(String payload) {
            buffer.append(payload);
            fragmentCount++;
        }

        String getContent() {
            return buffer.toString();
        }

        int size() {
            return buffer.length();
        }

        int getFragmentCount() {
            return fragmentCount;
        }

        boolean isExpired(int timeoutSeconds) {
            return Instant.now().isAfter(startTime.plusSeconds(timeoutSeconds));
        }
    }
}
