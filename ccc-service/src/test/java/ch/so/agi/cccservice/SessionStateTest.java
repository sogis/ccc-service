package ch.so.agi.cccservice;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class SessionStateTest {

    /*ToDo: Entfernen, da in setState integriert?
    @Test
    public void getState() {

    }*/

    @Test
    public void setState() {
        SessionState sessionState = new SessionState();

        sessionState.setState(sessionState.CONNECTED_TO_APP);

        String state = sessionState.getState();

        Assert.assertEquals(sessionState.CONNECTED_TO_APP, state);
    }

    /* ToDo: Entfernen, da in setAppName integriert?
    @Test
    public void getAppName() {
    }*/

    @Test
    public void setAppName() {
        String expectedAppName = "App-Name";
        SessionState sessionState = new SessionState();

        sessionState.setAppName(expectedAppName);

        String appName = sessionState.getAppName();

        Assert.assertEquals(expectedAppName, appName);
    }

    /* ToDo: Entfernen, da in setGisName integriert?
    @Test
    public void getGisName() {
    }
    */

    @Test
    public void setGisName() {
        String expectedGisName = "GIS-Name";
        SessionState sessionState = new SessionState();

        sessionState.setGisName(expectedGisName);

        String gisName = sessionState.getGisName();

        Assert.assertEquals(expectedGisName, gisName);
    }

    @Test
    public void gisNameEqualsNotAppName() {
        String expectedGisName = "GIS-Name";
        String expectedAppName = "App-Name";
        SessionState sessionState = new SessionState();

        sessionState.setAppName(expectedAppName);
        sessionState.setGisName(expectedGisName);

        String gisName = sessionState.getGisName();
        String appName = sessionState.getAppName();

        Assert.assertNotEquals(expectedAppName, gisName);
        Assert.assertNotEquals(expectedGisName, appName);
    }
}