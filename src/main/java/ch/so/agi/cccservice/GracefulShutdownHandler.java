package ch.so.agi.cccservice;

import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

/**
 * Handles graceful shutdown of WebSocket connections during Kubernetes Rolling Updates.
 * When the pod receives SIGTERM, this component ensures all WebSocket sessions are
 * properly closed before the application terminates.
 */
@Component
public class GracefulShutdownHandler {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownHandler.class);

    @PreDestroy
    public void onShutdown() {
        log.info("Graceful shutdown initiated - closing all WebSocket sessions");

        List<Session> openSessions = Sessions.openSessions();
        int sessionCount = openSessions.size();

        log.info("Found {} open sessions to close", sessionCount);

        for (Session session : openSessions) {
            closeSessionGracefully(session);
        }

        log.info("Graceful shutdown completed - closed {} sessions", sessionCount);
    }

    private void closeSessionGracefully(Session session) {
        try {
            log.debug("Closing session {} gracefully", session.getSessionNr());

            closeWebSocketConnection(session.getGisWebSocket(), session.getSessionNr(), "GIS");
            closeWebSocketConnection(session.getAppWebSocket(), session.getSessionNr(), "App");

        } catch (Exception e) {
            log.error("Unexpected error closing session {}: {}",
                    session.getSessionNr(), e.getMessage(), e);
        }
    }

    private void closeWebSocketConnection(WebSocketSession socket, int sessionNr, String connectionType) {
        if (socket != null && socket.isOpen()) {
            try {
                socket.close(CloseStatus.GOING_AWAY);
                log.debug("Session {}: {} connection closed", sessionNr, connectionType);
            } catch (IOException e) {
                log.error("Session {}: Failed to close {} connection: {}",
                        sessionNr, connectionType, e.getMessage());
            }
        }
    }
}
