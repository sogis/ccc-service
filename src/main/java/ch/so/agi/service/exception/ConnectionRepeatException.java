package ch.so.agi.service.exception;

/**
 * Raised when a client attempts to connect to a session that already has
 * an active connection for that role (app or GIS).
 */
public class ConnectionRepeatException extends ClientException {

    public ConnectionRepeatException(String exMessage) {
        this(exMessage, null);
    }

    public ConnectionRepeatException(String exMessage, Throwable cause) {
        super(409, exMessage, cause);
    }
}
