package ch.so.agi.service.deamon;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class KeyChangeSender {

    private static final int DELAY_MILLIS = 5 * 60 * 1000; // 5 minutes in milliseconds

    @Scheduled(fixedDelay = DELAY_MILLIS)
    public void sendKeyChange(){}
}
