package ch.so.agi.cccservice.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class WebServerPort implements ApplicationListener<WebServerInitializedEvent> {

    private static final Logger log = LoggerFactory.getLogger(WebServerPort.class);
    private static volatile int port = -1;

    private static void setPort(int port) {
        WebServerPort.port = port;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        setPort(event.getWebServer().getPort());

        log.info("Web server initialized to port {}", port);
    }

    public static int getPort() {
        return port;
    }
}

