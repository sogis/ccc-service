package ch.so.agi.cccservice.exception;

/**
 * Thrown when a new session would exceed the configured global session cap.
 * Not a security violation but a capacity/overload condition; the connection
 * is rejected with WebSocket close status SERVICE_OVERLOAD (1013).
 */
public class SessionCapacityExceededException extends RuntimeException {

    private final int currentCount;
    private final int maxSessions;

    public SessionCapacityExceededException(int currentCount, int maxSessions) {
        super("Session capacity reached (" + currentCount + "/" + maxSessions + ")");
        this.currentCount = currentCount;
        this.maxSessions = maxSessions;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getMaxSessions() {
        return maxSessions;
    }
}
