package ch.so.agi.service.exception;

/**
 * Raised when a message requires a fully established session but the
 * handshake has not been completed yet.
 */
public class HandshakeIncompleteException extends ClientException {

    public HandshakeIncompleteException(String exMessage) {
        this(exMessage, null);
    }

    public HandshakeIncompleteException(String exMessage, Throwable cause) {
        super(503, exMessage, cause);
    }
}
