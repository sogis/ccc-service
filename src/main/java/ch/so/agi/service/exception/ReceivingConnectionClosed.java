package ch.so.agi.service.exception;

/**
 * Raised when a message cannot be forwarded because the intended
 * receiving connection has already been closed.
 */
public class ReceivingConnectionClosed extends ClientException {
    public ReceivingConnectionClosed(String details) {
        super(410, "Can not forward message as the receiving connection is closed.", details);
    }
}
