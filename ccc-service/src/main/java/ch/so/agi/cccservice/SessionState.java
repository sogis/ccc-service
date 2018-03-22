package ch.so.agi.cccservice;



public class SessionState {
    private String appName;
    private String gisName;
    private String state;

    public static final String CONNECTED_TO_APP = "connected to app";
    public static final String CONNECTED_TO_GIS = "connected to gis";
    public static final String READY = "ready";

    public String getState() {
        return state;
    }

    public void setState(String state) {
        //prüfen, ob korrekter State übergeben wird?
        this.state = state;
    }

    public String getAppName(){
        return appName;
    }

    public void setAppName(String appName){
        this.appName = appName;
    }

    public String getGisName() {
        return gisName;
    }

    public void setGisName(String gisName) {
        this.gisName = gisName;
    }

}
