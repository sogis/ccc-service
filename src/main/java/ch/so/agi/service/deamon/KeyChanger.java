package ch.so.agi.service.deamon;

import ch.so.agi.service.message.KeyChange;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import ch.so.agi.service.session.SockConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeyChanger {

    private static final int DELAY_MILLIS = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final Logger log = LoggerFactory.getLogger(KeyChanger.class);

    @Scheduled(fixedDelay = DELAY_MILLIS)
    public void sendKeyChange(){
        List<Session> v2Sessions = Sessions.allSessions()
                .filter(Session::hasV2Connection).toList();

        for(Session ses : v2Sessions){
            for(SockConnection con : ses.v2Connections()){
                KeyChange.sendKeyChangeToConnection(con);
            }
        }

        String v2SessionsString = v2Sessions.stream()
                .map(ses -> Integer.toString(ses.getSessionNr()))
                .collect(Collectors.joining(", "));;

        log.info("Woke after sleeping {} sec. Sent keychange to the v2 connections of {} sessions. Session numbers: [{}].",
                Duration.ofMillis(DELAY_MILLIS).toSeconds(),
                v2Sessions.size(),
                v2SessionsString);
    }
}
