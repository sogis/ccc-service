package ch.so.agi.cccservice.exception;

/**
 * Exception thrown when a client exceeds the rate limit for reconnect attempts.
 */
public class RateLimitExceededException extends CccSecurityException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
