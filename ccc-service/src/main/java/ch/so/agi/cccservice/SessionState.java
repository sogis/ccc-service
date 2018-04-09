package ch.so.agi.cccservice;

import java.sql.Timestamp;

/**
 * The current state of one active ccc session.
 * A ccc sesssion is a pairing of a domain application and a gis application.
 */
public class SessionState {
    private String appName;
    private boolean appConnected = false;
    private long appConnectTime;
    private String gisName;
    private boolean gisConnected = false;
    private long gisConnectTime;
    private boolean readySent = false;

    /**
     * Sets SessionState to connected to App
     * @param clientName as String
     */
    public void addAppConnection(String clientName){
        this.appName = clientName;
        appConnected = true;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        appConnectTime = timestamp.getTime();
    }

    /**
     * Sets SessionState to connected to GIS
     * @param clientName as String
     */
    public void addGisConnection(String clientName){
        this.gisName = clientName;
        gisConnected = true;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        gisConnectTime = timestamp.getTime();
    }

    /**
     * Sets SessionState to sent Ready to GIS and App
     */
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

    /**
     * Reports if is connected to App
     * @return true/false if it is connected to App
     */
    public Boolean isAppConnected(){
        return appConnected;
    }

    /**
     * Reports if is connected to GIS
     * @return true/false if it is connected to GIS
     */
    public Boolean isGisConnected(){
        return gisConnected;
    }

    /**
     * Reports if Ready has been sent
     * @return true/false if ready has been sent
     */
    public Boolean isReadySent(){
        return readySent;
    }

    /**
     * Gets time when App has connected to CCC-Server
     * @return time on which appConnect has been received
     */
    public long getAppConnectTime() {
        return appConnectTime;
    }

    /**
     * Gets time when GIS has connected to CCC-Server
     * @return time on which gisConnect has been received
     */
    public long getGisConnectTime(){
        return gisConnectTime;
    }

    /**
     * Removes App from SessionState
     */
    public void removeAppConnection(){
        this.appName = null;
        appConnected = false;
        Timestamp timestamp = null;
    }

    /**
     * Removes GIS from SessionState
     */
    public void removeGisConnection(){
        this.gisName = null;
        gisConnected = false;
        Timestamp timestamp = null;
    }
}
