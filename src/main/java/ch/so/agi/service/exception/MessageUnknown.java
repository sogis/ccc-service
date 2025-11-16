package ch.so.agi.service.exception;

/**
 * Raised when a message references a method that is unknown to the server.
 */
public class MessageUnknown extends ClientException {
    public MessageUnknown(String method) {
        super(404, "The sent message type is not known.", method);
    }
}
