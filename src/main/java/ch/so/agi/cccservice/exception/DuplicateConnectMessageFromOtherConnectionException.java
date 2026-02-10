package ch.so.agi.cccservice.exception;

import java.util.UUID;

/**
 * Raised when a client attempts to connect to a session that already has
 * an active connection for that role (app or gis).
 */
public class DuplicateConnectMessageFromOtherConnectionException extends CccSecurityException {

    private final String debugMessage;

    public DuplicateConnectMessageFromOtherConnectionException(int sesNr, UUID sesUid, String duplicateClientName, String firstClientName) {
        super(String.format(
                "Session %d: Duplicate connection attempt from client '%s'. "
                + "The session uuid may have leaked. Original client: '%s'.",
                sesNr,
                duplicateClientName,
                firstClientName
                )
        );
        this.debugMessage = String.format(
                "Session %d: Duplicate connection from client '%s' with session uuid '%s'. Original client: '%s'.",
                sesNr,
                duplicateClientName,
                sesUid,
                firstClientName
        );
    }

    public String getDebugMessage() {
        return debugMessage;
    }
}


