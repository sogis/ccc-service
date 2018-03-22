package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class Service {

    private SessionPool sessionPool;
    private SocketSender sender;
    private JsonConverter jsonConverter = new JsonConverter();

    @Autowired
    public Service(SessionPool sessionPool, SocketSender sender){
        this.sessionPool = sessionPool;
        this.sender = sender;
    }

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
            //Errormessage --> keine Session im Sessionpool gefunden
            throw new Exception("No Session with sessionID " + sessionId.getSessionId() + " in sessionPool ");
        }
    }

    private void checkApiVersion(String apiVersion, SessionId sessionId, SocketSender sender, String connectionTyp){
        if (!"1.0".equals(apiVersion)){
            ErrorMessage errorMessage = new ErrorMessage(505, "Die API-Version " + apiVersion +
                    " wird nicht unterstützt. Muss 1.0 sein.");
            if (connectionTyp.equals("appConnect")) {
                sender.sendMessageToApp(sessionId, errorMessage);
                //Programmabbruch? --> SessionID aus SessionPool entfernen?
            } else if (connectionTyp.equals("gisConnect")){
                sender.sendMessageToGis(sessionId, errorMessage);
                //Programmabbruch? --> session aus sessionPool entfernen?
            }

            throw new IllegalArgumentException("Wrong API-Version used. Used API-Version: " + apiVersion);
        }
    }

    public void gisConnect(GisConnectMessage msg) {

        String clientName = msg.getClientName();
        SessionId sessionId = msg.getSession();
        String apiVersion = msg.getApiVersion();

        checkApiVersion(apiVersion, sessionId, sender, "gisConnect");

        SessionState appSessionState = sessionPool.getSession(sessionId);

        if (appSessionState != null) {
            String appState = appSessionState.getState();
            if (appState != appSessionState.CONNECTED_TO_APP) {
                //Error --> Verbindung besteht bereits oder appConnect fehlt
            } else {
                appSessionState.setState(appSessionState.CONNECTED_TO_GIS);
                appSessionState.setGisName(clientName);

            }
        } else {
            //Errormessage -> keine Session im Sessionpool gefunden
        }
        
    }
    public void ready(SessionId sessionId, ReadyMessage msg) throws Exception {
        WebSocketSession appWebSocket = sessionPool.getAppWebSocketSession(sessionId);
        WebSocketSession gisWebSocket = sessionPool.getGisWebSocketSession(sessionId);
        SessionState sessionState = sessionPool.getSession(sessionId);


        String readyTextMessage = jsonConverter.messageToString(msg);
        TextMessage textMessage = new TextMessage(readyTextMessage);

        //Verbindungsunterbruch abfangen?
        //Wie testen ohne Verbindung?
        appWebSocket.sendMessage(textMessage);
        gisWebSocket.sendMessage(textMessage);

        System.out.println(textMessage);
        sessionState.setState(sessionState.READY);

        
    }
    public void create(SessionState state, CreateMessage msg) {
        
    }
    public void edit(SessionState state, EditMessage msg) {
        
    }
    public void show(SessionState state, ShowMessage msg) {
        
    }
    public void cancel(SessionState state, CancelMessage msg) {
        
    }
    public void changed(SessionState state, ChangedMessage msg) {
        
    }
    public void selected(SessionState state, SelectedMessage msg) {
        
    }
    public void dataWritten(SessionState state, DataWrittenMessage msg) {

    }
    public void sendError(WebSocketSession webSocketSession, ErrorMessage msg){

        /*//ErrorMessage umwandeln
        String errorMessage = null;
        TextMessage errorTextMessage = new TextMessage(errorMessage);
        try {
            webSocketSession.sendMessage(errorTextMessage);
        } catch (IOException e) {
            throw new
        }*/
    }
}
