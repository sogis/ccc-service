package ch.so.agi.cccservice.http;

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
        int total = (int)Sessions.allSessions().count();
        int open = Sessions.openSessions().size();
        int partial = total - open;
        return String.format("%s%nSessions total: %d (open: %d, partial: %d)",
                VERSION, total, open, partial);
    }
}
