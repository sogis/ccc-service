package ch.so.agi.cccservice;

import java.sql.Timestamp;

/**
 * Handles Sessions with their state and their names for application and GIS
 */
public class SessionState {
    private String appName;
    private Boolean appConnected = false;
    private long appConnectTime;
    private String gisName;
    private Boolean gisConnected = false;
    private long gisConnectTime;
    private Boolean readySent = false;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());


    public void addAppConnection(String clientName){
        this.appName = clientName;
        appConnected = true;
        appConnectTime = timestamp.getTime();
    }

    public void addGisConnection(String clientName){
        this.gisName = clientName;
        gisConnected = true;
        gisConnectTime = timestamp.getTime();
    }

    public void setConnectionsToReady(){
        readySent = true;
    }

    /**
     * gets application name
     * @return name of application
     */
    public String getAppName(){
        return appName;
    }

    /**
     * gets GIS name
     * @return name of GIS
     */
    public String getGisName() {
        return gisName;
    }

    public Boolean isAppConnected(){
        return appConnected;
    }

    public Boolean isGisConnected(){
        return gisConnected;
    }

    public Boolean isReadySent(){
        return readySent;
    }

    public long getAppConnectTime() {
        return appConnectTime;
    }

    public long getGisConnectTime(){
        return gisConnectTime;
    }
}
