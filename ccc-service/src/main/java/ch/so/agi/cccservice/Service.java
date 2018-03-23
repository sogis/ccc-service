package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

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
     * When Application sents appConnect the SessionState will be set to connected to app
     * @param msg AppConnectMessage
     * @throws Exception when there is no session in the sessionPool with the same sessionID
     */
    public void appConnect(AppConnectMessage msg) throws Exception{

        String clientName = msg.getClientName();
        SessionId sessionId = msg.getSession();
        String apiVersion = msg.getApiVersion();

        checkApiVersion(apiVersion,sessionId, sender, "appConnect");

        SessionState appSessionState = sessionPool.getSession(sessionId);

        if (appSessionState != null) {
            String appState = appSessionState.getState();
            if (appState != null) {
                sender.sendMessageToApp(sessionId, new ErrorMessage(504, "Die Session besteht bereits."));
                throw new IllegalStateException("Connection already exists.");
            } else {
                appSessionState.setState(appSessionState.CONNECTED_TO_APP);
                appSessionState.setAppName(clientName);
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
    private void checkApiVersion(String apiVersion, SessionId sessionId, SocketSender sender, String connectionTyp){
        if (!"1.0".equals(apiVersion)){
            ErrorMessage errorMessage = new ErrorMessage(505, "Die API-Version " + apiVersion +
                    " wird nicht unterstützt. Muss API-Version 1.0 sein.");
            if (connectionTyp.equals("appConnect")) {
                sender.sendMessageToApp(sessionId, errorMessage);
                //ToDo: Programmabbruch? --> SessionID aus SessionPool entfernen?
            } else if (connectionTyp.equals("gisConnect")){
                sender.sendMessageToGis(sessionId, errorMessage);
                //ToDo: Programmabbruch? --> session aus sessionPool entfernen?
            }

            throw new IllegalArgumentException("Wrong API-Version used. Used API-Version: " + apiVersion);
        }
    }

    /**
     * When GIS sents gisConnect the SessionState will be set to connected to gis
     * @param msg
     */
    public void gisConnect(GisConnectMessage msg) {

        String clientName = msg.getClientName();
        SessionId sessionId = msg.getSession();
        String apiVersion = msg.getApiVersion();

        checkApiVersion(apiVersion, sessionId, sender, "gisConnect");

        SessionState appSessionState = sessionPool.getSession(sessionId);

        if (appSessionState != null) {
            String appState = appSessionState.getState();
            if (appState != appSessionState.CONNECTED_TO_APP) {
                //ToDo: Error --> Verbindung besteht bereits oder appConnect fehlt
            } else {
                appSessionState.setState(appSessionState.CONNECTED_TO_GIS);
                appSessionState.setGisName(clientName);

            }
        } else {
            //ToDo: Errormessage -> keine Session im Sessionpool gefunden
        }
        
    }

    /**
     *
     * @param sessionId
     * @param msg
     * @throws Exception
     */
    public void ready(SessionId sessionId, ReadyMessage msg) throws Exception {
        WebSocketSession appWebSocket = sessionPool.getAppWebSocketSession(sessionId);
        WebSocketSession gisWebSocket = sessionPool.getGisWebSocketSession(sessionId);
        SessionState sessionState = sessionPool.getSession(sessionId);


        String readyTextMessage = jsonConverter.messageToString(msg);
        TextMessage textMessage = new TextMessage(readyTextMessage);

        //ToDo: Verbindungsunterbruch abfangen?
        //ToDo: Wie testen ohne Verbindung?
        appWebSocket.sendMessage(textMessage);
        gisWebSocket.sendMessage(textMessage);

        System.out.println(textMessage);
        sessionState.setState(sessionState.READY);

        
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

    /**
     *
     * @param webSocketSession
     * @param msg
     */
    public void sendError(WebSocketSession webSocketSession, ErrorMessage msg){

        /* ToDo: erstellen
        //ErrorMessage umwandeln
        String errorMessage = null;
        TextMessage errorTextMessage = new TextMessage(errorMessage);
        try {
            webSocketSession.sendMessage(errorTextMessage);
        } catch (IOException e) {
            throw new
        }*/
    }
}
