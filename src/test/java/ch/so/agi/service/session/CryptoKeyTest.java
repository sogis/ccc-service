package ch.so.agi.service.session;

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

        try {
            Thread.sleep(990);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertTrue(key.isEqual(hash));
    }

    @Test
    public void testKeyIsNotEqualOutsideGraceperiod() {
        CryptoKey key = new CryptoKey(1);
        String hash = key.getKeyString();
        key.refreshKey();

        try {
            Thread.sleep(1010);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertFalse(key.isEqual(hash));
    }
}