package ch.so.agi.service.message;

import ch.so.agi.service.exception.ClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * Helper responsible for sending notifyError messages back to the client
 * whenever a {@link ClientException} occurs.
 */
public final class ErrorSender {
    private static final Logger log = LoggerFactory.getLogger(ErrorSender.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private ErrorSender() {
    }

    public static void send(WebSocketSession connection, ClientException exception) {
        if (connection == null) {
            return;
        }

        ObjectNode payload = mapper.createObjectNode();
        payload.put("method", Error.MESSAGE_TYPE);
        payload.put("code", exception.getCode());
        payload.put("message", exception.getMessage());
        payload.put("nativeCode", exception.getClass().getName());

        try {
            connection.sendMessage(new TextMessage(payload.toString()));
        } catch (IOException e) {
            log.warn("Could not send notifyError to client {}", connection.getId(), e);
        }
    }
}
