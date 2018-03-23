package ch.so.agi.cccservice;

/**
 * Handles Sessions with their state and their names for application and GIS
 */
public class SessionState {
    private String appName;
    private String gisName;
    private String state;

    public static final String CONNECTED_TO_APP = "connected to app";
    public static final String CONNECTED_TO_GIS = "connected to gis";
    public static final String READY = "ready";

    /**
     * gets state of connection of a specific SessionState
     * @return connectionsstate
     */
    public String getState() {
        return state;
    }

    /**
     * sets state of connection
     * @param state of connection
     */
    public void setState(String state) {
        //prüfen, ob korrekter State übergeben wird?
        this.state = state;
    }

    /**
     * gets application name
     * @return name of application
     */
    public String getAppName(){
        return appName;
    }

    /**
     * sets application name
     * @param appName name of application
     */
    public void setAppName(String appName){
        this.appName = appName;
    }

    /**
     * gets GIS name
     * @return name of GIS
     */
    public String getGisName() {
        return gisName;
    }

    /**
     * sets gis name
     * @param gisName name of gis
     */
    public void setGisName(String gisName) {
        this.gisName = gisName;
    }

}
