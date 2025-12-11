package ch.so.agi.cccservice.exception;

/**
 * Raised when a client attempts to connect to a session that already has
 * an active connection for that role (app or gis).
 */
public class DuplicateConnectMessageFromSameConnectionException extends ClientException {

    public DuplicateConnectMessageFromSameConnectionException(String exMessage) {
        this(exMessage, null);
    }

    public DuplicateConnectMessageFromSameConnectionException(String exMessage, Throwable cause) {
        super(409, exMessage, cause);
    }
}

