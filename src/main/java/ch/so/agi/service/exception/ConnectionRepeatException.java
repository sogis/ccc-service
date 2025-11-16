package ch.so.agi.service.exception;

import java.util.UUID;

/**
 * Raised when a client attempts to connect to a session that already has
 * an active connection for that role (app or GIS).
 */
public class ConnectionRepeatException extends ClientException {
    public ConnectionRepeatException(UUID sessionUid) {
        super(409, "Can not connect as connection already exists.", sessionUid == null ? null : sessionUid.toString());
    }
}
