package ch.so.agi.cccservice;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SessionStateTest {
    private String expectedGisName = "GIS-Name";
    private String expectedAppName = "App-Name";


    @Test
    public void gisNameEqualsNotAppName() {
        SessionState sessionState = new SessionState();

        sessionState.addAppConnection(expectedAppName);
        sessionState.addGisConnection(expectedGisName);

        String gisName = sessionState.getGisName();
        String appName = sessionState.getAppName();

        assertNotEquals(expectedAppName, gisName);
        assertNotEquals(expectedGisName, appName);
    }

    @Test
    public void addAppConnection() {
        SessionState sessionState = new SessionState();

        sessionState.addAppConnection(expectedAppName);

        assertEquals(expectedAppName, sessionState.getAppName());
        assertTrue(sessionState.isAppConnected());
        assertFalse(sessionState.isGisConnected());
        assertFalse(sessionState.isReadySent());

    }

    @Test
    public void addGisConnection() {
        SessionState sessionState = new SessionState();

        sessionState.addGisConnection(expectedGisName);

        assertEquals(expectedGisName, sessionState.getGisName());
        assertTrue(sessionState.isGisConnected());
        assertFalse(sessionState.isAppConnected());
        assertFalse(sessionState.isReadySent());
    }

    @Test
    public void setConnectionsToReady() {
        SessionState sessionState = new SessionState();

        sessionState.setConnectionsToReady();

        assertTrue(sessionState.isReadySent());
    }

    @Test
    public void getAppConnectTime() {
        SessionState sessionState = new SessionState();
        sessionState.addAppConnection(expectedAppName);

        assertTrue(sessionState.getAppConnectTime() > 0);
    }

    @Test
    public void getGisConnectTime() {

        SessionState sessionState = new SessionState();
        sessionState.addGisConnection(expectedGisName);

        assertTrue(sessionState.getGisConnectTime() > 0);
    }
}
