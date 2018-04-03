package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.Math.abs;

/**
 *
 */
@Component
public class Service {

    private SessionPool sessionPool;
    private SocketSender sender;

    @Autowired
    public Service(SessionPool sessionPool, SocketSender sender){
        this.sessionPool = sessionPool;
        this.sender = sender;
    }

    /**
     * Based on the type of message the correct method will be called
     * @param sessionId ID of session
     * @param message delivered Message
     * @throws ServiceException
     */
    public void handleMessage(SessionId sessionId, AbstractMessage message) throws ServiceException{

        if (message instanceof AppConnectMessage) {
            AppConnectMessage appConnectMessage = (AppConnectMessage) message;
            handleAppConnect(appConnectMessage);
        }

        if (message instanceof GisConnectMessage) {
            GisConnectMessage gisConnectMessage = (GisConnectMessage) message;
            handleGisConnect(gisConnectMessage);
        }

        if (message instanceof CancelMessage){
            CancelMessage cancelMessage = (CancelMessage) message;
            cancel(sessionId, cancelMessage);
        }

        if (message instanceof ChangedMessage){
            ChangedMessage changedMessage = (ChangedMessage) message;
            changed(sessionId, changedMessage);
        }

        if (message instanceof CreateMessage){
            CreateMessage createMessage = (CreateMessage) message;
            create(sessionId, createMessage);
        }

        if (message instanceof DataWrittenMessage){
            DataWrittenMessage dataWrittenMessage = (DataWrittenMessage) message;
            dataWritten(sessionId, dataWrittenMessage);
        }

        if (message instanceof EditMessage){
            EditMessage editMessage = (EditMessage) message;
            edit(sessionId, editMessage);
        }

        if (message instanceof SelectedMessage){
            SelectedMessage selectedMessage = (SelectedMessage) message;
            selected(sessionId, selectedMessage);
        }

        if (message instanceof ShowMessage){
            ShowMessage showMessage = (ShowMessage) message;
            show(sessionId, showMessage);
        }

        //ToDo: ErrorMessage
    }

    /**
     * When Application sents handleAppConnect the SessionState will be set to connected to app
     * @param msg AppConnectMessage
     * @throws ServiceException when there is no session in the sessionPool with the same sessionID
     */
    public void handleAppConnect(AppConnectMessage msg) throws ServiceException{

        String clientName = msg.getClientName();
        SessionId sessionId = msg.getSession();
        String apiVersion = msg.getApiVersion();
        ReadyMessage readyMessage = new ReadyMessage();
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
                    sendReady(sessionId, readyMessage);
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
        long maxAllowedTimeDifference = 60 * 1000;  //60 seconds

        if (timeDifference > maxAllowedTimeDifference){
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
     * When GIS sents handleGisConnect the SessionState will be set to connected to gis
     * @param msg GisConnectMessage sent from GIS
     */
    public void handleGisConnect(GisConnectMessage msg) throws ServiceException {

        String clientName = msg.getClientName();
        SessionId sessionId = msg.getSession();
        String apiVersion = msg.getApiVersion();
        boolean sessionTimeOut;
        ReadyMessage readyMessage = new ReadyMessage();

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
                    sendReady(sessionId, readyMessage);
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
    private void sendReady(SessionId sessionId, ReadyMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);
        msg.setApiVersion("1.0");

        //ToDo: Verbindungsunterbruch abfangen?
        sender.sendMessageToApp(sessionId, msg);
        sender.sendMessageToGis(sessionId, msg);

        sessionState.setConnectionsToReady();
    }

    /**
     * sends createMessage from App to GIS
     * @param sessionId ID of Session
     * @param msg createMessage
     */
    public void create(SessionId sessionId, CreateMessage msg) throws ServiceException {
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
    public void edit(SessionId sessionId, EditMessage msg) throws ServiceException{
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
    public void show(SessionId sessionId, ShowMessage msg) throws ServiceException {
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
    public void cancel(SessionId sessionId, CancelMessage msg) throws ServiceException {
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
    public void changed(SessionId sessionId, ChangedMessage msg) throws ServiceException {
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
    public void selected(SessionId sessionId, SelectedMessage msg) throws ServiceException {
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
    public void dataWritten(SessionId sessionId, DataWrittenMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        checkIfConnectionIsEstablished(sessionState);
        sender.sendMessageToGis(sessionId, msg);
    }
    public void error(SessionId sessionId, String typ, ErrorMessage msg) throws ServiceException {
        JsonConverter jsonConverter = new JsonConverter();
        System.out.println(typ);
        if (typ.equals("app")){
            sender.sendMessageToApp(sessionId, msg);
        } else if (typ.equals("gis")) {
            System.out.println("hier sollte er landen " + sessionId + " " + msg.toString() );
            sender.sendMessageToGis(sessionId, msg);
        }

        //sender.sendMessageToWebSocket(webSocketSession, msg);
}
}
