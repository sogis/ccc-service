package ch.so.agi.cccservice.deamon;

import ch.so.agi.cccservice.session.Sessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SessionsKiller {

    private static final Logger log = LoggerFactory.getLogger(SessionsKiller.class);

    private static final String CRON = "0 0 3 * * *";

    /**
     * Prevents overlapping cleanup runs. If a previous cleanup is still running
     * (e.g., due to many sessions or slow closes), the next scheduled run is skipped.
     */
    private final AtomicBoolean cleanupRunning = new AtomicBoolean(false);

    public SessionsKiller(){
        log.info("Session killer created. Runs every morning at three o clock. Cron conf: '{}'", CRON);
    }

    // Runs every day at 3:00 AM server time
    @Scheduled(cron = CRON)
    public void killAllSessions() {
        if (!cleanupRunning.compareAndSet(false, true)) {
            log.warn("Previous session cleanup is still running, skipping this run");
            return;
        }

        log.info("Triggering scheduled session cleanup...");

        // Run cleanup in a separate thread to avoid blocking the scheduler thread.
        // Even with timeouts in Session.closeConnections(), running in a separate thread
        // ensures the scheduler is never blocked and the timer continues to work.
        Thread cleanupThread = new Thread(() -> {
            try {
                int numSessions = Sessions.resetSessionCollection();
                log.info("Reset the session collection and killed {} old sessions.", numSessions);
            } catch (Exception e) {
                log.error("Error while resetting session collection", e);
            } finally {
                cleanupRunning.set(false);
            }
        }, "sessions-killer-cleanup");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }
}
