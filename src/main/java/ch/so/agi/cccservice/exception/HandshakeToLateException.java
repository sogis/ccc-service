package ch.so.agi.cccservice.exception;

/**
 * Raised when the second client connects after the allowed handshake window
 * and therefore the clients can no longer be paired.
 */
public class HandshakeToLateException extends ClientException {

    public HandshakeToLateException(String exMessage) {
        this(exMessage, null);
    }

    public HandshakeToLateException(String exMessage, Throwable cause) {
        super(504, exMessage, cause);
    }
}

