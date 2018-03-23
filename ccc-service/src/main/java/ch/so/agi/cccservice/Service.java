package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static java.lang.Math.abs;

/**
 *
 */
public class Service {

    private SessionPool sessionPool;
    private SocketSender sender;
    private JsonConverter jsonConverter = new JsonConverter();

    @Autowired
    public Service(SessionPool sessionPool, SocketSender sender){
        this.sessionPool = sessionPool;
        this.sender = sender;
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

        checkApiVersion(apiVersion,sessionId, sender, "handleAppConnect");

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
            throw new Exception("No Session with sessionID " + sessionId.getSessionId() + " in sessionPool ");
        }
    }

    /**
     * Checks it the apiVersion sent by the application/gis equals 1.0. Only apiVersion 1.0 is supported
     * @param apiVersion should be 1.0
     * @param sessionId to send ErrorMessage to the correct Client
     * @param sender to send ErrorMessage to the correct Client
     * @param connectionTyp to send ErrorMessage to the correct Client
     */
    private void checkApiVersion(String apiVersion, SessionId sessionId, SocketSender sender, String connectionTyp)
            throws ServiceException{

        if (!"1.0".equals(apiVersion)){
            throw new ServiceException(505, "Die API-Version " + apiVersion +
                    " wird nicht unterstützt. Muss API-Version 1.0 sein.");
        }
    }

    /**
     *
     * @param sessionState
     * @param sessionId
     * @throws ServiceException
     */
    private void checkForSessionTimeOut(SessionState sessionState, SessionId sessionId) throws ServiceException{
        long timeDifference = getTimeDifference(sessionState);

        if (timeDifference > 60 * 1000){
            throw new ServiceException(506, "Session-Timeout");
        } else {
            //ToDo: readyMessage definieren
            ReadyMessage readyMessage = new ReadyMessage();
            sendReady(sessionId, readyMessage);
        }
    }

    /**
     *
     * @param sessionState
     * @return
     */
    private long getTimeDifference(SessionState sessionState){
        long appConnectTime = sessionState.getAppConnectTime();
        long gisConnectTime = sessionState.getGisConnectTime();

        return abs(appConnectTime-gisConnectTime);
    }



    /**
     * When GIS sents handleGisConnect the SessionState will be set to connected to gis
     * @param msg
     */
    public void handleGisConnect(GisConnectMessage msg) throws ServiceException {

        String clientName = msg.getClientName();
        SessionId sessionId = msg.getSession();
        String apiVersion = msg.getApiVersion();

        checkApiVersion(apiVersion, sessionId, sender, "handleGisConnect");

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
            //ToDo: Errormessage -> keine Session im Sessionpool gefunden
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

        //ToDo: Verbindungsunterbruch abfangen?
        //ToDo: Wie testen ohne Verbindung?
        sender.sendMessageToApp(sessionId, msg);
        sender.sendMessageToGis(sessionId, msg);

        sessionState.setConnectionsToReady();

        
    }

    /**
     *
     * @param state
     * @param msg
     */
    public void create(SessionState state, CreateMessage msg) {
        
    }

    /**
     *
     * @param state
     * @param msg
     */
    public void edit(SessionState state, EditMessage msg) {
        
    }

    /**
     *
     * @param state
     * @param msg
     */
    public void show(SessionState state, ShowMessage msg) {
        
    }

    /**
     *
     * @param state
     * @param msg
     */
    public void cancel(SessionState state, CancelMessage msg) {
        
    }

    /**
     *
     * @param state
     * @param msg
     */
    public void changed(SessionState state, ChangedMessage msg) {
        
    }

    /**
     *
     * @param state
     * @param msg
     */
    public void selected(SessionState state, SelectedMessage msg) {
        
    }

    /**
     *
     * @param state
     * @param msg
     */
    public void dataWritten(SessionState state, DataWrittenMessage msg) {

    }

}
