package ch.so.agi.service.exception;

/**
 * Raised when an incoming message cannot be parsed into a valid CCC message.
 */
public class MessageMalformedException extends ClientException {
    public MessageMalformedException(String payload) {
        this(payload, null);
    }

    public MessageMalformedException(String payload, Throwable cause) {
        super(400, "The sent message json is malformed.", payload, cause);
    }
}
