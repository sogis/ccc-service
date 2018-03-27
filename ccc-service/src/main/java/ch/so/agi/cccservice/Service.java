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
     *
     * @param message
     * @throws Exception
     */
    public void handleMessage(SessionId sessionId, AbstractMessage message) throws Exception{

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
     * @throws Exception when there is no session in the sessionPool with the same sessionID
     */
    public void handleAppConnect(AppConnectMessage msg) throws Exception{

        String clientName = msg.getClientName();
        SessionId sessionId = msg.getSession();
        String apiVersion = msg.getApiVersion();

        checkApiVersion(apiVersion);

        SessionState sessionState = sessionPool.getSession(sessionId);

        if (sessionState != null) {
            if (!sessionState.isAppConnected()){
                sessionState.addAppConnection(clientName);
                if (sessionState.isGisConnected()){
                    checkForSessionTimeOut(sessionState, sessionId);
                }
            } else {
                throw new ServiceException(504, "Application is already connected.");
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
     * @param sessionState
     * @param sessionId
     * @throws ServiceException
     */
    private void checkForSessionTimeOut(SessionState sessionState, SessionId sessionId) throws ServiceException{
        long timeDifference = getTimeDifference(sessionState);
        long maxAllowedTimeDifference = 60 * 1000;  //60 seconds

        if (timeDifference > maxAllowedTimeDifference){
            throw new ServiceException(506, "Session-Timeout");
        } else {
            ReadyMessage readyMessage = new ReadyMessage();
            sendReady(sessionId, readyMessage);
        }
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
     * @param msg
     */
    public void handleGisConnect(GisConnectMessage msg) throws ServiceException {

        String clientName = msg.getClientName();
        SessionId sessionId = msg.getSession();
        String apiVersion = msg.getApiVersion();

        checkApiVersion(apiVersion);

        SessionState sessionState = sessionPool.getSession(sessionId);

        if (sessionState != null) {
            if (!sessionState.isGisConnected()){
                sessionState.addGisConnection(clientName);
                if (sessionState.isAppConnected()){
                    checkForSessionTimeOut(sessionState, sessionId);
                }
            } else {
                throw new ServiceException(504, "Application is already connected.");
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
     * @throws Exception
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
     *
     * @param sessionId
     * @param msg
     */
    public void create(SessionId sessionId, CreateMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        if (!sessionState.isReadySent()){
            throw new ServiceException(503, "No connection has been established");
        }
        sender.sendMessageToGis(sessionId, msg);
    }

    /**
     *
     * @param sessionId
     * @param msg
     */
    public void edit(SessionId sessionId, EditMessage msg) throws ServiceException{
        SessionState sessionState = sessionPool.getSession(sessionId);

        if (!sessionState.isReadySent()){
            throw new ServiceException(503, "No connection has been established");
        }
        sender.sendMessageToGis(sessionId, msg);
    }

    /**
     *
     * @param sessionId
     * @param msg
     */
    public void show(SessionId sessionId, ShowMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        if (!sessionState.isReadySent()){
            throw new ServiceException(503, "No connection has been established");
        }
        sender.sendMessageToGis(sessionId, msg);
    }

    /**
     *
     * @param sessionId
     * @param msg
     */
    public void cancel(SessionId sessionId, CancelMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        if (!sessionState.isReadySent()){
            throw new ServiceException(503, "No connection has been established");
        }
        sender.sendMessageToGis(sessionId, msg);
        
    }

    /**
     *
     * @param sessionId
     * @param msg
     */
    public void changed(SessionId sessionId, ChangedMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        if (!sessionState.isReadySent()){
            throw new ServiceException(503, "No connection has been established");
        }
        sender.sendMessageToApp(sessionId, msg);
        
    }

    /**
     *
     * @param sessionId
     * @param msg
     */
    public void selected(SessionId sessionId, SelectedMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        if (!sessionState.isReadySent()){
            throw new ServiceException(503, "No connection has been established");
        }
        sender.sendMessageToApp(sessionId, msg);
        
    }

    /**
     *
     * @param sessionId
     * @param msg
     */
    public void dataWritten(SessionId sessionId, DataWrittenMessage msg) throws ServiceException {
        SessionState sessionState = sessionPool.getSession(sessionId);

        if (!sessionState.isReadySent()){
            throw new ServiceException(503, "No connection has been established");
        }
        sender.sendMessageToGis(sessionId, msg);
    }

}
