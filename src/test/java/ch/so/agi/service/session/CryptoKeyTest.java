package ch.so.agi.service.session;

import ch.so.agi.service.TestUtil;
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
}