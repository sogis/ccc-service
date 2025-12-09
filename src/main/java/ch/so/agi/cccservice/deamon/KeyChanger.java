package ch.so.agi.cccservice.deamon;

import ch.so.agi.cccservice.message.KeyChange;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;
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

    public KeyChanger(){
        log.info(
                "Keychange service started. Will send keychanges to v 1.2 clients every {} seconds.",
                Duration.ofMillis(DELAY_MILLIS).toSeconds()
        );
    }

    @Scheduled(fixedDelay = DELAY_MILLIS, initialDelay = DELAY_MILLIS)
    public void sendKeyChange(){
        List<Session> v12Sessions = Sessions.allSessions()
                .filter(Session::hasV12Connection).toList();

        for(Session ses : v12Sessions){
            for(SockConnection con : ses.v12Connections()){
                if(con.isOpen())
                    KeyChange.sendKeyChangeToConnection(con);
            }
        }

        String v12SessionsString = v12Sessions.stream()
                .map(ses -> Integer.toString(ses.getSessionNr()))
                .collect(Collectors.joining(", "));;

        log.info(
                "Sent keychange to the v 1.2 connections of {} sessions. Session numbers: [{}].",
                v12Sessions.size(),
                v12SessionsString
        );
    }
}
