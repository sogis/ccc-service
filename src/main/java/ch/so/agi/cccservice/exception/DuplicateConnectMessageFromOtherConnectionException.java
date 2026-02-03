package ch.so.agi.cccservice.exception;

import java.util.UUID;

/**
 * Raised when a client attempts to connect to a session that already has
 * an active connection for that role (app or gis).
 */
public class DuplicateConnectMessageFromOtherConnectionException extends SecurityException {

    public DuplicateConnectMessageFromOtherConnectionException(int sesNr, UUID sesUid, String duplicateClientName, String firstClientName) {
        super(String.format(
                "Duplicate connection message received from client '%s'.%n"
                + "The session uuid '%s' of session '%s' could have leaked.%n"
                + "Original and connected client name is '%s'.",
                duplicateClientName,
                sesUid,
                sesNr,
                firstClientName
                )
        );
    }
}


