package ch.so.agi.cccservice;

import jakarta.servlet.ServletContext;
import org.apache.tomcat.websocket.server.WsServerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Lowers Tomcat's WebSocket background-check period so idle-timeout enforcement
 * does not lag the configured timeout by up to the default scan interval (10s).
 * With period=1, an idle session is closed within ~1s of its maxIdleTimeout
 * expiring, which makes short pre-Connect timeouts actually behave as configured.
 *
 * <p><b>Tomcat coupling:</b> This relies on the Tomcat-internal class
 * {@link WsServerContainer} and its non-standard
 * {@code setProcessPeriod(int)} method. There is no Spring Boot / Jakarta
 * WebSocket property that exposes this knob, so direct coupling is unavoidable.
 * A Tomcat major upgrade may rename or remove either, in which case the
 * {@code instanceof} guard below silently skips tuning (with a WARN log) and
 * Tomcat's default 10s scan period applies again. Re-validate after upgrades.
 */
@Component
public class TomcatWebSocketTuning {

    private static final Logger log = LoggerFactory.getLogger(
        TomcatWebSocketTuning.class
    );

    private static final String SERVER_CONTAINER_ATTR =
        "jakarta.websocket.server.ServerContainer";

    private final int idleCheckPeriodSeconds;

    public TomcatWebSocketTuning(
        @Value(
            "${ccc.websocket.idle-check-period-seconds:1}"
        ) int idleCheckPeriodSeconds
    ) {
        this.idleCheckPeriodSeconds = Math.max(1, idleCheckPeriodSeconds);
    }

    @EventListener
    public void onWebServerReady(WebServerInitializedEvent event) {
        if (
            !(event.getApplicationContext()
                instanceof ServletWebServerApplicationContext ctx)
        ) {
            return;
        }
        ServletContext servletContext = ctx.getServletContext();
        if (servletContext == null) {
            return;
        }
        Object attr = servletContext.getAttribute(SERVER_CONTAINER_ATTR);
        if (attr instanceof WsServerContainer wsc) {
            wsc.setProcessPeriod(idleCheckPeriodSeconds);
            log.info(
                "Tomcat WebSocket idle-check period set to {}s",
                idleCheckPeriodSeconds
            );
        } else {
            log.warn(
                "ServerContainer is not a Tomcat WsServerContainer ({}); idle-check period not tuned",
                attr == null ? "null" : attr.getClass().getName()
            );
        }
    }
}
