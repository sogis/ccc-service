package ch.so.agi.service.messages.exceptions;

import org.springframework.core.ExceptionDepthComparator;

/**
 * Thrown when a incoming message can not be parsed and converted
 * to one of the Messages of the CCC-Protocol.
 */
public class MessageParseException extends RuntimeException {
    public MessageParseException(String receivedString) {
        this(receivedString, null);
    }

    public MessageParseException(String receivedString, Exception inner) {
        super("Could not parse the incoming message. Message: " + receivedString, inner);
    }
}
