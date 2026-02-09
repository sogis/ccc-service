package ch.so.agi.cccservice.session;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;

/**
 * Class representing a cryptographic key with a validation function
 * accepting equality to all past keys issued inside the last x
 * seconds defined in gracePeriodInSeconds.
 */
public final class CryptoKey {

    private final int gracePeriodInSeconds;
    private final ArrayDeque<IssuedKey> issuedKeys = new ArrayDeque<>();

    private record IssuedKey(String key, Instant timestamp) {}

    /**
     * Key validity: KeyChanger.DELAY_MILLIS (5 min) + 1 minute grace period = 6 minutes total.
     * Must be greater than KeyChanger.DELAY_MILLIS to allow reconnects after key rotation.
     */
    public static final int DEFAULT_GRACE_PERIOD_SECONDS = 360;

    public CryptoKey() {
        this(DEFAULT_GRACE_PERIOD_SECONDS);
    }

    /**
     * Constructor used only for testing
     */
    CryptoKey(int gracePeriodInSeconds) {
        this.gracePeriodInSeconds = gracePeriodInSeconds;
        refreshKey();
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH_BYTES = 16; // 128 bits

    public synchronized String getKeyString() {
        return issuedKeys.peekFirst().key();
    }

    public synchronized void refreshKey() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String token = Base64.getEncoder().encodeToString(tokenBytes);
        // newest key goes to the FRONT
        issuedKeys.addFirst(new IssuedKey(token, Instant.now()));
    }

    public synchronized boolean isEqual(String key) {
        // cleanup stale keys first
        Instant cutoff = Instant.now().minusSeconds(gracePeriodInSeconds);
        issuedKeys.removeIf(k -> k.timestamp().isBefore(cutoff));

        return issuedKeys.stream().anyMatch(k -> k.key().equals(key));
    }
}
