package ch.so.agi.cccservice.http;

import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.so.agi.cccservice.session.Sessions;

@RestController
@RequestMapping(produces = MediaType.TEXT_PLAIN_VALUE)
public class StatusPage {
    private final BuildProperties buildProperties;

    public StatusPage(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/")
    public String statusInfo() {
        return "CCC-Service Version " + buildProperties.getVersion()
                + System.lineSeparator() + Sessions.sessionStats();
    }
}
