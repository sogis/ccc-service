package ch.so.agi.cccservice.message;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.so.agi.cccservice.exception.ClientException;

/**
 * Helper responsible for sending notifyError messages back to the client
 * whenever a {@link ClientException} occurs.
 */
public final class ErrorSender {
    private static final Logger log = LoggerFactory.getLogger(ErrorSender.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private ErrorSender() {
    }

    // Deliberately unsynchronized: mapper.createObjectNode() creates a fresh instance per
    // call, so there is no shared mutable state to protect. A lock here would serialize
    // error delivery for ALL sessions on a single monitor, so one slow/unresponsive client
    // blocking inside sendMessage() would stall error delivery for every unrelated session
    // too (see ErrorSenderConcurrencyTest).
    public static void send(WebSocketSession connection, ClientException exception) {
        if (connection == null) {
            return;
        }

        ObjectNode payload = mapper.createObjectNode();
        payload.put("method", ErrorMessage.MESSAGE_TYPE);
        payload.put("code", exception.getCode());
        payload.put("message", exception.getMessage());
        payload.put("nativeCode", exception.getClass().getName());

        try {
            connection.sendMessage(new TextMessage(payload.toString()));
        } catch (IOException e) {
            if (!connection.isOpen()) {
                log.debug("Could not send notifyError to client {}: connection already closed", connection.getId());
            } else {
                log.warn("Could not send notifyError to client {}", connection.getId(), e);
            }
        }
    }
}
