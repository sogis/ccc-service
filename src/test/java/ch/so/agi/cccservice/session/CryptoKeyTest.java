package ch.so.agi.cccservice.session;

import ch.so.agi.cccservice.TestUtil;
import ch.so.agi.cccservice.deamon.KeyChanger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CryptoKeyTest {

    @Test
    public void testgetKeyString() {
        CryptoKey key = new CryptoKey();
        assertNotNull(key.getKeyString());
    }

    @Test
    public void testRefreshKey() {
        CryptoKey key = new CryptoKey();
        String first = key.getKeyString();
        key.refreshKey();
        String second = key.getKeyString();
        assertNotEquals(first, second);
    }

    @Test
    public void testKeyIsEqualInsideGraceperiod() {
        CryptoKey key = new CryptoKey(1);
        String hash = key.getKeyString();
        key.refreshKey();

        TestUtil.wait(990);

        assertTrue(key.isEqual(hash));
    }

    @Test
    public void testKeyIsNotEqualOutsideGraceperiod() {
        CryptoKey key = new CryptoKey(1);
        String hash = key.getKeyString();
        key.refreshKey();

        TestUtil.wait(1010);

        assertFalse(key.isEqual(hash));
    }

    /**
     * Ensures that the grace period is longer than the key rotation interval.
     * This is critical: if grace period <= rotation interval, clients could fail
     * to reconnect after a key change.
     *
     * Required: GracePeriod > KeyRotationInterval + ReconnectBuffer
     * Current:  360s (6 min) > 300s (5 min) + 60s (1 min buffer)
     */
    @Test
    public void gracePeriodMustExceedKeyRotationInterval() {
        int keyRotationIntervalSeconds = KeyChanger.DELAY_MILLIS / 1000;
        int gracePeriodSeconds = CryptoKey.DEFAULT_GRACE_PERIOD_SECONDS;
        int minimumGraceBuffer = 60; // 1 minute buffer for reconnects

        assertTrue(
                gracePeriodSeconds >= keyRotationIntervalSeconds + minimumGraceBuffer,
                String.format(
                        "Grace period (%ds) must be >= key rotation interval (%ds) + buffer (%ds). " +
                        "Otherwise clients may fail to reconnect after key rotation.",
                        gracePeriodSeconds, keyRotationIntervalSeconds, minimumGraceBuffer
                )
        );
    }
}