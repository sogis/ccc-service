package ch.so.agi.service.exception;

/**
 * Raised when a message references a method that is unknown to the server.
 */
public class MessageUnknownException extends ClientException {

    public MessageUnknownException(String exMessage) {
        this(exMessage, null);
    }

    public MessageUnknownException(String exMessage, Throwable cause) {
        super(404, exMessage, cause);
    }
}
