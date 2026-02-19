package ch.so.agi.cccservice.http;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.so.agi.cccservice.session.Sessions;

@RestController
@RequestMapping(produces = MediaType.TEXT_PLAIN_VALUE)
public class StatusPage {
    private static final String VERSION = "CCC-Service Version 1.2";

    @GetMapping("/")
    public String statusInfo() {
        return VERSION + System.lineSeparator() + Sessions.sessionStats();
    }
}
