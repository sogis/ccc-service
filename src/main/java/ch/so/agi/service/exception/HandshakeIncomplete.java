package ch.so.agi.service.exception;

/**
 * Raised when a message requires a fully established session but the
 * handshake has not been completed yet.
 */
public class HandshakeIncomplete extends ClientException {
    public HandshakeIncomplete(String details) {
        super(503, "Sent message can not be processed as handshake is not complete.", details);
    }
}
