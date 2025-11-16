package ch.so.agi.service.exception;

/**
 * Raised when a message references a method that is unknown to the server.
 */
public class MessageUnknownException extends ClientException {
    public MessageUnknownException(String method) {
        super(404, "The sent message type is not known.", method);
    }
}
