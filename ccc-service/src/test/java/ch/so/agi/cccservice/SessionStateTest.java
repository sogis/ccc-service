package ch.so.agi.cccservice;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

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

        Assert.assertNotEquals(expectedAppName, gisName);
        Assert.assertNotEquals(expectedGisName, appName);
    }

    @Test
    public void addAppConnection() {
        SessionState sessionState = new SessionState();

        sessionState.addAppConnection(expectedAppName);

        Assert.assertEquals(expectedAppName, sessionState.getAppName());
        Assert.assertTrue(sessionState.isAppConnected());
        Assert.assertFalse(sessionState.isGisConnected());
        Assert.assertFalse(sessionState.isReadySent());

    }

    @Test
    public void addGisConnection() {
        SessionState sessionState = new SessionState();

        sessionState.addGisConnection(expectedGisName);

        Assert.assertEquals(expectedGisName, sessionState.getGisName());
        Assert.assertTrue(sessionState.isGisConnected());
        Assert.assertFalse(sessionState.isAppConnected());
        Assert.assertFalse(sessionState.isReadySent());
    }

    @Test
    public void setConnectionsToReady() {
        SessionState sessionState = new SessionState();

        sessionState.setConnectionsToReady();

        Assert.assertTrue(sessionState.isReadySent());
    }

    @Test
    public void getAppConnectTime() {
        SessionState sessionState = new SessionState();
        sessionState.addAppConnection(expectedAppName);

        Assert.assertTrue(sessionState.getAppConnectTime() > 0);
    }

    @Test
    public void getGisConnectTime() {

        SessionState sessionState = new SessionState();
        sessionState.addGisConnection(expectedGisName);

        Assert.assertTrue(sessionState.getGisConnectTime() > 0);
    }
}