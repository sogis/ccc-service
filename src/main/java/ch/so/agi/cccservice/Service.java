package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.Math.abs;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Implements the main logic of the ccc-protocol.
 * Implements, what happens if a message has been sent to the ccc-server. 
 * 
 */
@Component
public class Service {
    public static final String CCC_MAX_INACTIVITY = "CCC_MAX_INACTIVITY";
    public static final String CCC_MAX_PAIRING = "CCC_MAX_PAIRING";
    public static final String CCC_PING_INTERVAL ="CCC_PING_INTERVAL";
    public static final int APP=1;
    public static final int GIS=2;

    private SessionPool sessionPool;
    private SocketSender sender;
    private Logger logger = LoggerFactory.getLogger(Service.class);
    public static final long DEFAULT_MAX_INACTIVITYTIME = 2L*60L*60L; // 2 hours
    public static final long DEFAULT_MAX_PAIRINGTIME = 60L;  // 60 seconds
    public static final long DEFAULT_PING_INTERVAL = 300L;  // 300 seconds
    private Long maxInactivityTime=null;
    private Long maxPairingTime=null;
    private Long pingIntervalTime=null;
    
    @Autowired
    public Service(SessionPool sessionPool, SocketSender sender){
        this.sessionPool = sessionPool;
        this.sender = sender;
    }

    /**
     * Based on the type of message the correct method will be called
     * @param sessionId ID of session
     * @param message delivered Message
     * @throws ServiceException on Exception
     */
    public void handleAppMessage(SessionId sessionId, AbstractMessage message) throws ServiceException{
        sessionPool.checkActivityTimeout(sessionId,getMaxInactivityTime());
        if (message instanceof ConnectAppMessage) {
            ConnectAppMessage appConnectMessage = (ConnectAppMessage) message;
            handleConnectApp(appConnectMessage);
        }else if (message instanceof CancelEditGeoObjectMessage){
            CancelEditGeoObjectMessage cancelMessage = (CancelEditGeoObjectMessage) message;
            handleCancelEditGeoObject(sessionId, cancelMessage);
        }else if (message instanceof CreateGeoObjectMessage){
            CreateGeoObjectMessage createMessage = (CreateGeoObjectMessage) message;
            handleCreateGeoObject(sessionId, createMessage);
        }else if (message instanceof NotifyObjectUpdatedMessage){
            NotifyObjectUpdatedMessage dataWrittenMessage = (NotifyObjectUpdatedMessage) message;
            handleNotifyObjectUpdated(sessionId, dataWrittenMessage);
        }else if (message instanceof EditGeoObjectMessage){
            EditGeoObjectMessage editMessage = (EditGeoObjectMessage) message;
            handleEditGeoObject(sessionId, editMessage);
        }else if (message instanceof ShowGeoObjectMessage){
            ShowGeoObjectMessage showMessage = (ShowGeoObjectMessage) message;
            handleShowGeoObject(sessionId, showMessage);
        }else if (message instanceof NotifyErrorMessage){
            NotifyErrorMessage errorMessage = (NotifyErrorMessage) message;
            handleError(APP,sessionId, errorMessage);
        }else {
            throw new ServiceException(500,"unexpected message");
        }
    }
    /**
     * Based on the type of message the correct method will be called
     * @param sessionId ID of session
     * @param message delivered Message
     * @throws ServiceException on Exception
     */
    public void handleGisMessage(SessionId sessionId, AbstractMessage message) throws ServiceException{
        sessionPool.checkActivityTimeout(sessionId,getMaxInactivityTime());
        if (message instanceof ConnectGisMessage) {
            ConnectGisMessage gisConnectMessage = (ConnectGisMessage) message;
            handleConnectGis(gisConnectMessage);
        }else if (message instanceof NotifyEditGeoObjectDoneMessage){
            NotifyEditGeoObjectDoneMessage changedMessage = (NotifyEditGeoObjectDoneMessage) message;
            handleNotifyEditGeoObjectDone(sessionId, changedMessage);
        }else if (message instanceof NotifyGeoObjectSelectedMessage){
            NotifyGeoObjectSelectedMessage selectedMessage = (NotifyGeoObjectSelectedMessage) message;
            handleNotifyGeoObjectSelected(sessionId, selectedMessage);
        }else if (message instanceof NotifyErrorMessage){
            NotifyErrorMessage errorMessage = (NotifyErrorMessage) message;
            handleError(GIS,sessionId, errorMessage);
        }else {
            throw new ServiceException(500,"unexpected message");
        }
    }

    private void handleError(int messageSource, SessionId sessionId, NotifyErrorMessage errorMessage) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        checkIfConnectionIsEstablished(sessionState);
        if(messageSource==APP) {
            sender.sendMessageToGis(sessionId, errorMessage);
        }else if(messageSource==GIS) {
            sender.sendMessageToApp(sessionId, errorMessage);
        }else {
            throw new ServiceException(500,"unexpected client "+messageSource);
        }
    }

    /**
     * When Application sents handleAppConnect the SessionState will be set to connected to app
     * @param msg AppConnectMessage
     * @throws ServiceException when there is no session in the sessionPool with the same sessionID
     */
    private void handleConnectApp(ConnectAppMessage msg) throws ServiceException{

        String clientName = msg.getClientName();
        SessionId sessionId = msg.getSession();
        String apiVersion = msg.getApiVersion();
        NotifySessionReadyMessage readyMessage = new NotifySessionReadyMessage();
        boolean sessionTimeOut;

        checkApiVersion(apiVersion);

        SessionState sessionState = sessionPool.getSession(sessionId);

        if (sessionState != null) {
            if (sessionState.isAppConnected()){
                throw new ServiceException(504, "Application is already connected.");
            }
            sessionState.addAppConnection(clientName);
            if (sessionState.isGisConnected()){
                sessionTimeOut = checkForSessionTimeOut(sessionState);
                if (!sessionTimeOut) {
                    sendNotifySessionReady(sessionId, readyMessage);
                }
            }
        } else {
            sessionState = new SessionState();

            sessionPool.addSession(sessionId, sessionState);
            sessionState.addAppConnection(clientName);
        }
    }

    /**
     * Checks it the apiVersion sent by the application/gis equals 1.0. Only apiVersion 1.0 is supported
     * @param apiVersion should be 1.0
     */
    private void checkApiVersion(String apiVersion)
            throws ServiceException{

        String allowedApiVersion = "1.0";

        if (!allowedApiVersion.equals(apiVersion)){
            throw new ServiceException(505, "Die API-Version " + apiVersion +
                    " wird nicht unterstützt. Muss API-Version 1.0 sein.");
        }
    }

    /**
     * Checks if too much time passed between appConnect and gisConnect (allowed max. 60 seconds)
     * @param sessionState State of Session, which contains times of appConnect/gisConnect
     * @throws ServiceException Session-Timeout (time passed >60 seconds)
     */
    private boolean checkForSessionTimeOut(SessionState sessionState) throws ServiceException{
        long timeDifference = getTimeDifference(sessionState);
        long maxAllowedTimeDifference = getMaxPairingTime() * 1000;  //60 seconds

        if (timeDifference > maxAllowedTimeDifference){
            removeSessionFromSessionStateOnTimeOut(sessionState);
            throw new ServiceException(506, "Session-Timeout");
        }

        return false;
    }
    
    /**
     * Calculates time passed between appConnect and gisConnect
     * @param sessionState of specific session
     * @return time difference
     */
    private long getTimeDifference(SessionState sessionState){
        long appConnectTime = sessionState.getAppConnectTime();
        long gisConnectTime = sessionState.getGisConnectTime();

        return abs(appConnectTime - gisConnectTime);
    }


    /**
     * Removes Session from SessionState on timeout
     * @param sessionState
     * @throws ServiceException
     */
    private void removeSessionFromSessionStateOnTimeOut(SessionState sessionState) {
        long appConnectTime = sessionState.getAppConnectTime();
        long gisConnectTime = sessionState.getGisConnectTime();

        if (appConnectTime > gisConnectTime) {
            sessionState.removeAppConnection();
        } else if (gisConnectTime > appConnectTime) {
            sessionState.removeGisConnection();
        }
    }



    /**
     * When GIS sents handleGisConnect the SessionState will be set to connected to gis
     * @param msg GisConnectMessage sent from GIS
     */
    private void handleConnectGis(ConnectGisMessage msg) throws ServiceException {

        String clientName = msg.getClientName();
        SessionId sessionId = msg.getSession();
        String apiVersion = msg.getApiVersion();
        boolean sessionTimeOut;
        NotifySessionReadyMessage readyMessage = new NotifySessionReadyMessage();

        checkApiVersion(apiVersion);

        SessionState sessionState = sessionPool.getSession(sessionId);

        if (sessionState != null) {
            if (sessionState.isGisConnected()){
                throw new ServiceException(504, "Application is already connected.");
            }
            sessionState.addGisConnection(clientName);
            if (sessionState.isAppConnected()){
                sessionTimeOut = checkForSessionTimeOut(sessionState);
                if (!sessionTimeOut) {
                    sendNotifySessionReady(sessionId, readyMessage);
                }
            }
        } else {
            sessionState = new SessionState();

            sessionPool.addSession(sessionId, sessionState);
            sessionState.addGisConnection(clientName);
        }
    }

    /**
     * When connections to both client exist, send them sendReady-Message.
     * @param sessionId sessionID of this specific connection
     * @param msg Ready-Message
     */
    private void sendNotifySessionReady(SessionId sessionId, NotifySessionReadyMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);
        msg.setApiVersion("1.0");

        sender.sendMessageToApp(sessionId, msg);
        sender.sendMessageToGis(sessionId, msg);

        sessionState.setConnectionsToReady();
    }

    /**
     * sends createMessage from App to GIS
     * @param sessionId ID of Session
     * @param msg createMessage
     */
    private void handleCreateGeoObject(SessionId sessionId, CreateGeoObjectMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        checkIfConnectionIsEstablished(sessionState);
        sender.sendMessageToGis(sessionId, msg);
    }

    /**
     * checks if sessionState is initalized of if initialized sessionState has readySent = true
     * @param sessionState state of session
     * @throws ServiceException on missing sessionState or if ready has not been sent
     */
    private void checkIfConnectionIsEstablished(SessionState sessionState) throws ServiceException{
        if (sessionState ==null || !sessionState.isReadySent()){
            throw new ServiceException(503, "No connection has been established");
        }
    }

    /**
     * sends editMessage from App to GIS
     * @param sessionId ID of Session
     * @param msg editMessage
     * @throws ServiceException on missing sessionState or if ready has not been sent
     */
    private void handleEditGeoObject(SessionId sessionId, EditGeoObjectMessage msg) throws ServiceException{
        SessionState sessionState = sessionPool.getSession(sessionId);

        checkIfConnectionIsEstablished(sessionState);
        sender.sendMessageToGis(sessionId, msg);
    }

    /**
     * sends showMessage from App to GIS
     * @param sessionId ID of session
     * @param msg showMessage
     * @throws ServiceException on missing sessionState or if ready has not been sent
     */
    private void handleShowGeoObject(SessionId sessionId, ShowGeoObjectMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        checkIfConnectionIsEstablished(sessionState);
        sender.sendMessageToGis(sessionId, msg);
    }

    /**
     * sends cancelMessage from App to GIS
     * @param sessionId ID of session
     * @param msg cancelMessage
     * @throws ServiceException on missing sessionState or if ready has not been sent
     */
    private void handleCancelEditGeoObject(SessionId sessionId, CancelEditGeoObjectMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        checkIfConnectionIsEstablished(sessionState);
        sender.sendMessageToGis(sessionId, msg);
        
    }

    /**
     * sends changedMessage from GIS to App
     * @param sessionId ID of session
     * @param msg changedMessage
     * @throws ServiceException on missing sessionState or if ready has not been sent
     */
    private void handleNotifyEditGeoObjectDone(SessionId sessionId, NotifyEditGeoObjectDoneMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        checkIfConnectionIsEstablished(sessionState);
        sender.sendMessageToApp(sessionId, msg);
        
    }

    /**
     * sends selectedMessage from GIS to App
     * @param sessionId ID of session
     * @param msg selectedMessage
     * @throws ServiceException on missing sessionState or if ready has not been sent
     */
    private void handleNotifyGeoObjectSelected(SessionId sessionId, NotifyGeoObjectSelectedMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        checkIfConnectionIsEstablished(sessionState);
        sender.sendMessageToApp(sessionId, msg);
        
    }

    /**
     * sends dataWrittenMessage from App to GIS
     * @param sessionId ID of session
     * @param msg dataWrittenMessage
     * @throws ServiceException on missing sessionState or if ready has not been sent
     */
    private void handleNotifyObjectUpdated(SessionId sessionId, NotifyObjectUpdatedMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        checkIfConnectionIsEstablished(sessionState);
        sender.sendMessageToGis(sessionId, msg);
    }

    public long getMaxInactivityTime() {
        if(maxInactivityTime==null) {
            maxInactivityTime=DEFAULT_MAX_INACTIVITYTIME;
            String cccMaxInactivity=System.getProperty(CCC_MAX_INACTIVITY);
            if(cccMaxInactivity!=null) {
                try {
                    maxInactivityTime=Long.parseLong(cccMaxInactivity);
                    logger.debug(CCC_MAX_INACTIVITY+" <"+maxInactivityTime+">");
                } catch (NumberFormatException e) {
                    logger.warn("illegal formatted "+CCC_MAX_INACTIVITY+" <"+cccMaxInactivity+">");
                }
            }
        }
        return maxInactivityTime;
    }
    public long getMaxPairingTime() {
        if(maxPairingTime==null) {
            maxPairingTime=DEFAULT_MAX_PAIRINGTIME;
            String cccMaxPairing=System.getProperty(CCC_MAX_PAIRING);
            if(cccMaxPairing!=null) {
                try {
                    maxPairingTime=Long.parseLong(cccMaxPairing);
                    logger.debug(CCC_MAX_PAIRING+" <"+maxPairingTime+">");
                } catch (NumberFormatException e) {
                    logger.warn("illegal formatted "+CCC_MAX_PAIRING+" <"+cccMaxPairing+">");
                }
            }
        }
        return maxPairingTime;
    }

    public long getPingIntervalTime() {
        if(pingIntervalTime==null) {
            pingIntervalTime=DEFAULT_PING_INTERVAL;
            String cccPingInterval=System.getProperty(CCC_PING_INTERVAL);
            if(cccPingInterval!=null) {
                try {
                    pingIntervalTime=Long.parseLong(cccPingInterval);
                    logger.debug(CCC_PING_INTERVAL+" <"+pingIntervalTime+">");
                } catch (NumberFormatException e) {
                    logger.warn("illegal formatted "+CCC_PING_INTERVAL+" <"+cccPingInterval+">");
                }
            }
        }
        return pingIntervalTime;
    }
}
