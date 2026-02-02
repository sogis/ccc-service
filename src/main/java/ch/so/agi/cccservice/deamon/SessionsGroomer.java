package ch.so.agi.cccservice.deamon;

import ch.so.agi.cccservice.message.MessageAccumulator;
import ch.so.agi.cccservice.session.Sessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionsGroomer {

    private static final int DELAY_MILLIS = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final Logger log = LoggerFactory.getLogger(SessionsGroomer.class);

    private final MessageAccumulator messageAccumulator;

    public SessionsGroomer(MessageAccumulator messageAccumulator){
        this.messageAccumulator = messageAccumulator;
        log.info(
                "Session cleanup service started. Will cleanup stale sessions every {} seconds.",
                Duration.ofMillis(DELAY_MILLIS).toSeconds()
        );
    }

    @Scheduled(fixedDelay = DELAY_MILLIS, initialDelay = DELAY_MILLIS)
    public void removeStaleSessions(){

        List<Integer> sesNrStream = Sessions.removeStaleSessions().toList();

        String cleanedSessions = sesNrStream.stream().map(String::valueOf).collect(Collectors.joining(", "));

        log.info("Closed and removed {} stale sessions. Session numbers: [{}].",
                sesNrStream.size(),
                cleanedSessions);

        int purgedBuffers = messageAccumulator.purgeExpired();
        if (purgedBuffers > 0) {
            log.info("Purged {} expired partial message buffers.", purgedBuffers);
        }
    }
}

