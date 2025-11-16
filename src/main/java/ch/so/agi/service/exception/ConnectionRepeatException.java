package ch.so.agi.service.exception;

import ch.so.agi.service.message.Message;

import java.util.UUID;

/**
 * Raised when a client attempts to connect to a session that already has
 * an active connection for that role (app or GIS).
 */
public class ConnectionRepeatException extends ClientException {
    public ConnectionRepeatException(Message message, UUID sessionUid, boolean isAppConnection) {
        super(409, "Can not connect as connection already exists.", buildDetails(message, sessionUid, isAppConnection));
    }

    private static String buildDetails(Message message, UUID sessionUid, boolean isAppConnection) {
        String target = isAppConnection ? "app" : "gis";
        String session = sessionUid == null ? "<unknown>" : sessionUid.toString();
        return String.format("%s tried to connect %s client for session %s although the connection already exists.",
                Message.describe(message), target, session);
    }
}
