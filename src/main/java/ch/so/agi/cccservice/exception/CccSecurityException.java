package ch.so.agi.cccservice.exception;

/**
 * Base class for all security exceptions.
 * Security exception should never be raised in operations and are
 * logged as error.
 */
public abstract class CccSecurityException extends RuntimeException {

    protected CccSecurityException(String exMessage) {
        super(exMessage);
    }
}

