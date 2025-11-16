package ch.so.agi.service.exception;

/**
 * Raised when a message cannot be forwarded because the intended
 * receiving connection has already been closed.
 */
public class ReceivingConnectionClosedException extends ClientException {

    public ReceivingConnectionClosedException(String exMessage) {
        this(exMessage, null);
    }

    public ReceivingConnectionClosedException(String exMessage, Throwable cause) {
        super(410, exMessage, cause);
    }
}
