package ch.so.agi.service.exception;

import java.util.UUID;

/**
 * Raised when the second client connects after the allowed handshake window
 * and therefore the clients can no longer be paired.
 */
public class HandshakeToLateException extends ClientException {
    public HandshakeToLateException(UUID sessionUid) {
        super(504, "Can not pair the clients as the connectApp/connectGis message is too late.",
                sessionUid == null ? null : sessionUid.toString());
    }
}
