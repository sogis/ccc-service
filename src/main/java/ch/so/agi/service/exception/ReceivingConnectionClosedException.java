package ch.so.agi.service.exception;

import ch.so.agi.service.message.Message;

/**
 * Raised when a message cannot be forwarded because the intended
 * receiving connection has already been closed.
 */
public class ReceivingConnectionClosedException extends ClientException {
    public ReceivingConnectionClosedException(Message message, String details) {
        super(410, "Can not forward message as the receiving connection is closed.", enrichDetails(message, details));
    }

    public ReceivingConnectionClosedException(String details) {
        this(null, details);
    }

    private static String enrichDetails(Message message, String details) {
        if (message == null) {
            return details;
        }
        return String.format("%s (message=%s)", details, Message.describe(message));
    }
}
