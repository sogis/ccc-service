package ch.so.agi.service.exception;

import ch.so.agi.service.message.Message;

/**
 * Raised when a message requires a fully established session but the
 * handshake has not been completed yet.
 */
public class HandshakeIncompleteException extends ClientException {
    public HandshakeIncompleteException(Message message, String details) {
        super(503, "Sent message can not be processed as handshake is not complete.", enrichDetails(message, details));
    }

    public HandshakeIncompleteException(String details) {
        this(null, details);
    }

    private static String enrichDetails(Message message, String details) {
        if (message == null) {
            return details;
        }
        return String.format("%s (message=%s)", details, Message.describe(message));
    }
}
