package ch.so.agi.cccservice.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class WebServerPort implements ApplicationListener<WebServerInitializedEvent> {

    private static final Logger log = LoggerFactory.getLogger(WebServerPort.class);
    private static WebServerPort instance;

    private int port;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
        instance = this;

        log.info("Web server initialized to port {}", this.port);
    }

    public static int getPort() {
        if(instance == null)
            return -1;

        return instance.port;
    }
}

