package ch.so.agi.cccservice.deamon;

import ch.so.agi.cccservice.session.Sessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SessionsKiller {

    private static final Logger log = LoggerFactory.getLogger(SessionsKiller.class);

    private static final String CRON = "0 0 3 * * *";

    public SessionsKiller(){
        log.info("Session killer created. Runs every morning at three o clock. Cron conf: '{}'", CRON);
    }

    // Runs every day at 3:00 AM server time
    @Scheduled(cron = CRON)
    public void killAllSessions() {
        int numSessions = Sessions.resetSessionCollection();
        log.info("Reset the session collection and killed {} old sessions.", numSessions);
    }
}

