package ch.so.agi.cccservice;

import ch.so.agi.cccservice.session.Sessions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.TEXT_PLAIN_VALUE)
public class VersionController {
    private static final String VERSION = "ccc-service version 1.1";

    @GetMapping("/version")
    public String versionInfo() {
        long sessionCount = Sessions.allSessions().count();
        return String.format("%s\nSession count: %d", VERSION, sessionCount);
    }
}
