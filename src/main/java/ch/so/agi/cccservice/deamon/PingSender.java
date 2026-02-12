package ch.so.agi.cccservice.deamon;

import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;

@Service
public class PingSender {

    private static final int DELAY_MILLIS = 30 * 1000; // 30 seconds in milliseconds
    private static final Logger log = LoggerFactory.getLogger(PingSender.class);

    public PingSender(){
        log.info(
                "Ping sender service started. Will ping open sessions every {} seconds.",
                Duration.ofMillis(DELAY_MILLIS).toSeconds()
        );
    }

    @Scheduled(fixedDelay = DELAY_MILLIS, initialDelay = DELAY_MILLIS)
    public int pingConnections(){

        List<Session> sessions = Sessions.openSessions();

        for(Session s : sessions){
            s.getAppConnection().sendPing();
            s.getGisConnection().sendPing();
        }

        String sessionNrs = sessions.stream()
                .map(s -> String.valueOf(s.getSessionNr()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        log.info("Sent ping to {} sessions: [{}].", sessions.size(), sessionNrs);
        return sessions.size();
    }
}