package ch.so.agi.service.exception;

/**
 * Raised when an incoming message cannot be parsed into a valid CCC message.
 */
public class MessageMalformedException extends ClientException {
    public MessageMalformedException(String exMessage) {
        this(exMessage, null);
    }

    public MessageMalformedException(String exMessage, Throwable cause) {
        super(400, exMessage, cause);
    }
}
