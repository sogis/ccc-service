package ch.so.agi.cccservice.http;

import ch.so.agi.cccservice.health.LivenessProbe;
import ch.so.agi.cccservice.health.TestClient;
import ch.so.agi.cccservice.session.Sessions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.TEXT_PLAIN_VALUE)
public class StatusPage {
    private static final String VERSION = "CCC-Service Version 1.2";

    @GetMapping("/")
    public String statusInfo() {
        int sessionCount = (int)Sessions.allSessions().count() / 2;
        return String.format("%s%nAnzahl Sessions: %d", VERSION, sessionCount);
    }
}
