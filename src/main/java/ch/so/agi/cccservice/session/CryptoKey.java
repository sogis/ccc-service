package ch.so.agi.cccservice.session;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;

/**
 * Class representing a cryptographic key with a validation function
 * accepting equality to all past keys issued inside the last x
 * seconds defined in gracePeriodInSeconds.
 */
public class CryptoKey {

    private final int gracePeriodInSeconds;
    private final ArrayDeque<IssuedKey> issuedKeys = new ArrayDeque<>();

    private record IssuedKey(String key, Instant timestamp) {}

    public CryptoKey() {
        this(60);
    }

    /**
     * Constructor used only for testing
     */
    CryptoKey(int gracePeriodInSeconds) {
        this.gracePeriodInSeconds = gracePeriodInSeconds;
        refreshKey();
    }

    private static String encode(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public synchronized String getKeyString() {
        return issuedKeys.peekFirst().key();
    }

    public synchronized void refreshKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey newKey = keyGen.generateKey();
            // newest key goes to the FRONT
            issuedKeys.addFirst(new IssuedKey(encode(newKey), Instant.now()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized boolean isEqual(String key) {
        // cleanup stale keys first
        Instant cutoff = Instant.now().minusSeconds(gracePeriodInSeconds);
        issuedKeys.removeIf(k -> k.timestamp().isBefore(cutoff));

        return issuedKeys.stream().anyMatch(k -> k.key().equals(key));
    }
}
