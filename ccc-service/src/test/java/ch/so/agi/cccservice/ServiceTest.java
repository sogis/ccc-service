package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AbstractMessage;
import ch.so.agi.cccservice.messages.AppConnectMessage;
import ch.so.agi.cccservice.messages.GisConnectMessage;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServiceTest {

    private SessionPool sessionPool = new SessionPool();
    private String expectedAppName = "App-Name";
    private String expectedGisName = "GIS-Name";
    private String sessionString = "{123-456-789-0}";
    private String apiVersion = "1.0";
    private SocketSenderDummy socketSender = new SocketSenderDummy();


    @Test
    public void appConnect() throws Exception {
        SessionId sessionId = new SessionId(sessionString);

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);

        String appName = sessionState.getAppName();

        Assert.assertTrue(sessionState.isAppConnected());
        Assert.assertFalse(sessionState.isGisConnected());
        Assert.assertFalse(sessionState.isReadySent());
        Assert.assertEquals(expectedAppName,appName);

    }

    private AppConnectMessage generateAppConnectMessage(SessionId sessionId, String apiVersion){
        AppConnectMessage appConnectMessage = new AppConnectMessage();
        appConnectMessage.setClientName(expectedAppName);
        appConnectMessage.setSession(sessionId);
        appConnectMessage.setApiVersion(apiVersion);

        return appConnectMessage;
    }

    @Test
    public void gisConnect() throws Exception {
        SessionId sessionId = new SessionId(sessionString);

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleGisConnect(gisConnectMessage);

        String gisName = sessionState.getGisName();

        Assert.assertTrue(sessionState.isGisConnected());
        Assert.assertFalse(sessionState.isAppConnected());
        Assert.assertFalse(sessionState.isReadySent());
        Assert.assertEquals(expectedGisName, gisName);
    }

    private GisConnectMessage generateGisConnectMessage(SessionId sessionId, String apiVersion){
        GisConnectMessage gisConnectMessage = new GisConnectMessage();
        gisConnectMessage.setClientName(expectedGisName);
        gisConnectMessage.setSession(sessionId);
        gisConnectMessage.setApiVersion(apiVersion);

        return gisConnectMessage;
    }


    @Test
    public void readySentToApp() throws Exception {
        SessionId sessionId = new SessionId(sessionString);
        JsonConverter jsonConverter = new JsonConverter();

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        service.handleGisConnect(gisConnectMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();

        Assert.assertTrue(appMessages.size() == 1);
        String appMessage = jsonConverter.messageToString(appMessages.get(0));

        Assert.assertEquals(appMessage, "{\"method\":\"ready\",\"apiVersion\":\"1.0\"}");
    }

    @Test
    public void readySentToGis() throws Exception {
        SessionId sessionId = new SessionId(sessionString);
        JsonConverter jsonConverter = new JsonConverter();

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        service.handleGisConnect(gisConnectMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 1);
        String gisMessage = jsonConverter.messageToString(gisMessages.get(0));

        Assert.assertEquals(gisMessage, "{\"method\":\"ready\",\"apiVersion\":\"1.0\"}");
    }

    @Test (expected = ServiceException.class)
    public void failWithSessionTimeOut() throws Exception{
        SessionId sessionId = new SessionId(sessionString);

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        TimeUnit.SECONDS.sleep(62);

        service.handleGisConnect(gisConnectMessage);
    }

    @Test
    public void create() {
    }

    @Test
    public void edit() {
    }

    @Test
    public void show() {
    }

    @Test
    public void cancel() {
    }

    @Test
    public void changed() {
    }

    @Test
    public void selected() {
    }

    @Test
    public void dataWritten() {
    }

    @Test (expected=ServiceException.class)
    public void failsWithWrongApiVersionOnAppConnect() throws Exception{
        SessionId sessionId = new SessionId(sessionString);

        String apiVersion = "2.0";

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);
        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);


    }

    @Test (expected=ServiceException.class)
    public void failsWithWrongApiVersionOnGisConnect() throws Exception{
        SessionId sessionId = new SessionId(sessionString);

        String wrongApiVersion= "2.0";

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);
        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, wrongApiVersion);

        service.handleGisConnect(gisConnectMessage);


    }
}