package ch.so.agi.cccservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UuidTest {
    @Test
    public void valid()
    {
        assertTrue(SessionId.isValidUuid("{235ea7d3-8069-4bbc-b7de-17ff15239e7c}"));
    }
    @Test
    public void withoutBraces_fail()
    {
        assertFalse(SessionId.isValidUuid("235ea7d3-8069-4bbc-b7de-17ff15239e7c"));
    }
    @Test
    public void empty_fail()
    {
        assertFalse(SessionId.isValidUuid(""));
    }
    @Test
    public void toShort_fail()
    {
        assertFalse(SessionId.isValidUuid("{235ea7d3-8069-4bbc-17ff15239e7c}"));
    }
    @Test
    public void wrongChar_fail()
    {
        assertFalse(SessionId.isValidUuid("{235ea7d3-8069-4bbc-b7de-17ff15239e7x}"));
    }

}
