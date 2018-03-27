package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServiceTest {

    private SessionPool sessionPool = new SessionPool();
    private String expectedAppName = "App-Name";
    private String expectedGisName = "GIS-Name";
    private String sessionString = "{123-456-789-0}";
    //private SessionId = new SessionId();
    private String apiVersion = "1.0";
    private String readyString = "{\"method\":\"ready\",\"apiVersion\":\"1.0\"}";
    private String createString =
            "{\"method\":\"create\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"zoomTo\":{\"gemeinde\":2542}}";
    private String editString =
            "{\"method\":\"edit\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"data\":{\"type\":\"Point\"," +
                    "\"coordinates\":\"[2609190,1226652]\"}}";
    private String showString = "{\"method\":\"show\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
    private String cancelString = "{\"method\":\"cancel\",\"context\":{\"afu_geschaeft\":\"3671951\"}}";
    private String changedString = "{\"method\":\"changed\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
    private String selectedString = "{\"method\":\"selected\",\"context_list\":[{\"bfs_num\":231,\"parz_num\":1951}," +
            "{\"bfs_num\":231,\"parz_num\":2634}]}";
    private String dataWrittenString = "{\"method\":\"dataWritten\",\"properties\":{\"laufnr\":\"2017-820\"," +
            "\"grundbuch\":\"Trimbach\"}}";


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
        JsonConverter jsonConverter = new JsonConverter();

        establishConnection();

        List<AbstractMessage> appMessages = socketSender.getAppMessages();

        Assert.assertTrue(appMessages.size() == 1);
        String appMessage = jsonConverter.messageToString(appMessages.get(0));

        Assert.assertEquals(appMessage, readyString);
    }

    private Service establishConnection() throws Exception{
        SessionId sessionId = new SessionId(sessionString);

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        service.handleGisConnect(gisConnectMessage);

        return service;
    }

    @Test
    public void readySentToGis() throws Exception {
        JsonConverter jsonConverter = new JsonConverter();

        establishConnection();

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 1);
        String gisMessage = jsonConverter.messageToString(gisMessages.get(0));

        Assert.assertEquals(gisMessage, readyString);
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
    public void create() throws Exception{
        JsonConverter jsonConverter = new JsonConverter();
        SessionId sessionId = new SessionId(sessionString);

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        service.handleGisConnect(gisConnectMessage);

        CreateMessage createMessage = (CreateMessage) jsonConverter.stringToMessage(createString);
        service.create(sessionId, createMessage);


        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);
        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));

        Assert.assertEquals(createString, gisMessage);
    }

    @Test
    public void edit() throws Exception {
        JsonConverter jsonConverter = new JsonConverter();
        SessionId sessionId = new SessionId(sessionString);

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        service.handleGisConnect(gisConnectMessage);

        EditMessage editMessage = (EditMessage) jsonConverter.stringToMessage(editString);
        service.edit(sessionId, editMessage);


        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);
        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));

        Assert.assertEquals(editString, gisMessage);
    }

    @Test
    public void show() throws Exception {
        JsonConverter jsonConverter = new JsonConverter();
        SessionId sessionId = new SessionId(sessionString);

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        service.handleGisConnect(gisConnectMessage);

        ShowMessage showMessage = (ShowMessage) jsonConverter.stringToMessage(showString);
        service.show(sessionId, showMessage);


        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);
        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));

        Assert.assertEquals(showString, gisMessage);
    }

    @Test
    public void cancel() throws Exception {
        JsonConverter jsonConverter = new JsonConverter();
        SessionId sessionId = new SessionId(sessionString);

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        service.handleGisConnect(gisConnectMessage);

        CancelMessage cancelMessage = (CancelMessage) jsonConverter.stringToMessage(cancelString);
        service.cancel(sessionId, cancelMessage);


        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);
        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));

        Assert.assertEquals(cancelString, gisMessage);
    }

    @Test
    public void changed() throws Exception{
        JsonConverter jsonConverter = new JsonConverter();
        SessionId sessionId = new SessionId(sessionString);

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        service.handleGisConnect(gisConnectMessage);

        ChangedMessage changedMessage = (ChangedMessage) jsonConverter.stringToMessage(changedString);
        service.changed(sessionId, changedMessage);
        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);
        String gisMessage = jsonConverter.messageToString(appMessages.get(1));

        Assert.assertEquals(changedString, gisMessage);
    }

    @Test
    public void selected() throws Exception{
        JsonConverter jsonConverter = new JsonConverter();
        SessionId sessionId = new SessionId(sessionString);

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        service.handleGisConnect(gisConnectMessage);

        SelectedMessage selectedMessage = (SelectedMessage) jsonConverter.stringToMessage(selectedString);
        service.selected(sessionId, selectedMessage);
        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);
        String gisMessage = jsonConverter.messageToString(appMessages.get(1));

        Assert.assertEquals(selectedString, gisMessage);
    }

    @Test
    public void dataWritten() throws Exception {
        JsonConverter jsonConverter = new JsonConverter();
        SessionId sessionId = new SessionId(sessionString);

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        AppConnectMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        GisConnectMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        service.handleGisConnect(gisConnectMessage);

        DataWrittenMessage dataWrittenMessage = (DataWrittenMessage) jsonConverter.stringToMessage(dataWrittenString);
        service.dataWritten(sessionId, dataWrittenMessage);


        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);
        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));

        Assert.assertEquals(dataWrittenString, gisMessage);
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

    /*
    @Test
    public void handleMessageDataWritten() throws Exception{
        JsonConverter jsonConverter = new JsonConverter();
        Service service = establishConnection();


        DataWrittenMessage dataWrittenMessage = (DataWrittenMessage) jsonConverter.stringToMessage(dataWrittenString);
        service.handleMessage(sessionId, dataWrittenMessage);


        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);
        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));

        Assert.assertEquals(dataWrittenString, gisMessage);

    }*/
}