package ch.so.agi.service.exception;

/**
 * Raised when a v1 protocol client tries to reconnect
 */
public class ForbiddenReconnectException extends ClientException {

    public ForbiddenReconnectException(String exMessage) {
        this(exMessage, null);
    }

    public ForbiddenReconnectException(String exMessage, Throwable cause) {
        super(410, exMessage, cause);
    }
}
