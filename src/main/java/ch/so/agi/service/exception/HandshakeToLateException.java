package ch.so.agi.service.exception;

import ch.so.agi.service.message.Message;

import java.util.UUID;

/**
 * Raised when the second client connects after the allowed handshake window
 * and therefore the clients can no longer be paired.
 */
public class HandshakeToLateException extends ClientException {
    public HandshakeToLateException(Message message, UUID sessionUid) {
        super(504, "Can not pair the clients as the connectApp/connectGis message is too late.",
                buildDetails(message, sessionUid));
    }

    private static String buildDetails(Message message, UUID sessionUid) {
        String session = sessionUid == null ? "<unknown>" : sessionUid.toString();
        return String.format("%s connected after the handshake window for session %s elapsed.",
                Message.describe(message), session);
    }
}
