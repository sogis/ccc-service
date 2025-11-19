package ch.so.agi.service.deamon;

import ch.so.agi.service.session.Sessions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SessionCleanup {

    private static final int DELAY_MILLIS = 5 * 60 * 1000; // 5 minutes in milliseconds

    @Scheduled(fixedDelay = DELAY_MILLIS)
    public void removeStaleSessions(){
        Sessions.removeStaleSessions();
    }
}

