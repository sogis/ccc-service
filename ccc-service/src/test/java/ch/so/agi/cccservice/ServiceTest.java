package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServiceTest {

    private SessionPool sessionPool = new SessionPool();
    private String expectedAppName = "App-Name";
    private String expectedGisName = "GIS-Name";
    private String sessionString = "{123-456-789-0}";
    private SessionId sessionId = new SessionId(sessionString);
    private String apiVersion = "1.0";
    private String readyString = "{\"method\":\""+NotifySessionReadyMessage.METHOD_NAME+"\",\"apiVersion\":\"1.0\"}";
    private String createString =
            "{\"method\":\""+CreateGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"zoomTo\":{\"gemeinde\":2542}}";
    private String editString =
            "{\"method\":\""+EditGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"data\":{\"type\":\"Point\"," +
                    "\"coordinates\":\"[2609190,1226652]\"}}";
    private String showString = "{\"method\":\""+ShowGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
    private String cancelString = "{\"method\":\""+CancelEditGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}}";
    private String changedString = "{\"method\":\""+NotifyEditGeoObjectDoneMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
    private String selectedString = "{\"method\":\""+NotifyGeoObjectSelectedMessage.METHOD_NAME+"\",\"context_list\":[{\"bfs_num\":231,\"parz_num\":1951}," +
            "{\"bfs_num\":231,\"parz_num\":2634}]}";
    private String dataWrittenString = "{\"method\":\""+NotifyObjectUpdatedMessage.METHOD_NAME+"\",\"properties\":{\"laufnr\":\"2017-820\"," +
            "\"grundbuch\":\"Trimbach\"}}";
    private String errorString = "{\"method\":\""+NotifyErrorMessage.METHOD_NAME+"\",\"code\":999,\"message\":\"test Errormessage\"," +
            "\"userData\":{\"test\":\"3671951\"},\"nativeCode\":\"test nativeCode\"," +
            "\"technicalDetails\":\"test technicalDetails\"}";
    private SocketSenderDummy socketSender = new SocketSenderDummy();
    private JsonConverter jsonConverter = new JsonConverter();


    @Test
    public void appConnectMethodTest() throws Exception {

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);

        String appName = sessionState.getAppName();

        Assert.assertTrue(sessionState.isAppConnected());
        Assert.assertFalse(sessionState.isGisConnected());
        Assert.assertFalse(sessionState.isReadySent());
        Assert.assertEquals(expectedAppName,appName);

    }

    private ConnectAppMessage generateAppConnectMessage(SessionId sessionId, String apiVersion){
        ConnectAppMessage appConnectMessage = new ConnectAppMessage();
        appConnectMessage.setClientName(expectedAppName);
        appConnectMessage.setSession(sessionId);
        appConnectMessage.setApiVersion(apiVersion);

        return appConnectMessage;
    }

    @Test
    public void gisConnectMethodTest() throws Exception {

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        ConnectGisMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleGisConnect(gisConnectMessage);

        String gisName = sessionState.getGisName();

        Assert.assertTrue(sessionState.isGisConnected());
        Assert.assertFalse(sessionState.isAppConnected());
        Assert.assertFalse(sessionState.isReadySent());
        Assert.assertEquals(expectedGisName, gisName);
    }

    private ConnectGisMessage generateGisConnectMessage(SessionId sessionId, String apiVersion){
        ConnectGisMessage gisConnectMessage = new ConnectGisMessage();
        gisConnectMessage.setClientName(expectedGisName);
        gisConnectMessage.setSession(sessionId);
        gisConnectMessage.setApiVersion(apiVersion);

        return gisConnectMessage;
    }


    @Test
    public void basicConnectionTestGisAfterApp() throws Exception {

        establishConnection();

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 1);

        String appMessage = jsonConverter.messageToString(appMessages.get(0));
        Assert.assertEquals(appMessage, readyString);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 1);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(0));
        Assert.assertEquals(gisMessage, readyString);
    }

    private Service establishConnection() throws Exception{

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        ConnectGisMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        service.handleGisConnect(gisConnectMessage);

        return service;
    }

    @Test
    public void basicConnectionTestAppAfterGis() throws Exception {

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        ConnectGisMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleGisConnect(gisConnectMessage);
        service.handleAppConnect(appConnectMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 1);

        String appMessage = jsonConverter.messageToString(appMessages.get(0));
        Assert.assertEquals(appMessage, readyString);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 1);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(0));
        Assert.assertEquals(gisMessage, readyString);
    }



    @Test (expected = ServiceException.class)
    public void failsWithSessionTimeOut() throws Exception{

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        ConnectGisMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        TimeUnit.SECONDS.sleep(62);

        service.handleGisConnect(gisConnectMessage);
    }

    @Test (expected = ServiceException.class)
    public void failsWithSessionTimeOutOnDoubleAppConnect() throws Exception{

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        ConnectGisMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleGisConnect(gisConnectMessage);
        TimeUnit.SECONDS.sleep(62);

        try {
            service.handleAppConnect(appConnectMessage);
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
        }

        service.handleAppConnect(appConnectMessage);
    }

    @Test (expected = ServiceException.class)
    public void failsWithSessionTimeOutOnDoubleGisConnect() throws Exception{

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        ConnectGisMessage gisConnectMessage = generateGisConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);
        TimeUnit.SECONDS.sleep(62);

        try {
            service.handleGisConnect(gisConnectMessage);
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
        }

        service.handleGisConnect(gisConnectMessage);
    }

    @Test
    public void createMethodTest() throws Exception{

        Service service = establishConnection();

        CreateGeoObjectMessage createMessage = (CreateGeoObjectMessage) jsonConverter.stringToMessage(createString);
        service.create(sessionId, createMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(createString, gisMessage);
    }

    @Test
    public void editMethodTest() throws Exception {

        Service service = establishConnection();

        EditGeoObjectMessage editMessage = (EditGeoObjectMessage) jsonConverter.stringToMessage(editString);
        service.edit(sessionId, editMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(editString, gisMessage);
    }

    @Test
    public void useCaseShow() throws Exception {

        Service service = establishConnection();

        ShowGeoObjectMessage showMessage = (ShowGeoObjectMessage) jsonConverter.stringToMessage(showString);
        service.show(sessionId, showMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(showString, gisMessage);
    }

    @Test
    public void cancelMethodTest() throws Exception {

        Service service = establishConnection();

        CancelEditGeoObjectMessage cancelMessage = (CancelEditGeoObjectMessage) jsonConverter.stringToMessage(cancelString);
        service.cancel(sessionId, cancelMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(cancelString, gisMessage);
    }

    @Test
    public void changedMethodTest() throws Exception{

        Service service = establishConnection();

        NotifyEditGeoObjectDoneMessage changedMessage = (NotifyEditGeoObjectDoneMessage) jsonConverter.stringToMessage(changedString);
        service.changed(sessionId, changedMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(appMessages.get(1));
        Assert.assertEquals(changedString, gisMessage);
    }

    @Test
    public void useCaseSelected() throws Exception{

        Service service = establishConnection();

        NotifyGeoObjectSelectedMessage selectedMessage = (NotifyGeoObjectSelectedMessage) jsonConverter.stringToMessage(selectedString);
        service.selected(sessionId, selectedMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(appMessages.get(1));
        Assert.assertEquals(selectedString, gisMessage);
    }

    @Test
    public void dataWrittenMethodTest() throws Exception {

        Service service = establishConnection();

        NotifyObjectUpdatedMessage dataWrittenMessage = (NotifyObjectUpdatedMessage) jsonConverter.stringToMessage(dataWrittenString);
        service.dataWritten(sessionId, dataWrittenMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(dataWrittenString, gisMessage);
    }

    @Test (expected=ServiceException.class)
    public void failsWithWrongApiVersionOnAppConnect() throws Exception{

        String apiVersion = "2.0";

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);
        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);


    }


    @Test (expected=ServiceException.class)
    public void failsWithWrongApiVersionOnGisConnect() throws Exception{

        String wrongApiVersion= "2.0";

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);
        ConnectAppMessage appConnectMessage = generateAppConnectMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppConnect(appConnectMessage);

        ConnectGisMessage gisConnectMessage = generateGisConnectMessage(sessionId, wrongApiVersion);

        service.handleGisConnect(gisConnectMessage);


    }


    @Test
    public void handleMessageDataWritten() throws Exception{

        Service service = establishConnection();

        NotifyObjectUpdatedMessage dataWrittenMessage = (NotifyObjectUpdatedMessage) jsonConverter.stringToMessage(dataWrittenString);
        service.handleAppMessage(sessionId, dataWrittenMessage);


        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(dataWrittenString, gisMessage);

    }

    @Test
    public void handleMessageCancel() throws Exception {

        Service service = establishConnection();

        CancelEditGeoObjectMessage cancelMessage = (CancelEditGeoObjectMessage) jsonConverter.stringToMessage(cancelString);
        service.handleAppMessage(sessionId, cancelMessage);


        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(cancelString, gisMessage);

    }

    @Test
    public void handleMessageSelected() throws Exception{

        Service service = establishConnection();

        NotifyGeoObjectSelectedMessage selectedMessage = (NotifyGeoObjectSelectedMessage) jsonConverter.stringToMessage(selectedString);
        service.handleGisMessage(sessionId, selectedMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(appMessages.get(1));
        Assert.assertEquals(selectedString, gisMessage);
    }

    @Test
    public void handleMessageChanged() throws Exception{

        Service service = establishConnection();

        NotifyEditGeoObjectDoneMessage changedMessage = (NotifyEditGeoObjectDoneMessage) jsonConverter.stringToMessage(changedString);
        service.handleGisMessage(sessionId, changedMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(appMessages.get(1));
        Assert.assertEquals(changedString, gisMessage);
    }

    @Test
    public void handleMessageCreate() throws Exception{

        Service service = establishConnection();

        CreateGeoObjectMessage createMessage = (CreateGeoObjectMessage) jsonConverter.stringToMessage(createString);
        service.handleAppMessage(sessionId, createMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(createString, gisMessage);
    }

    @Test
    public void handleMessageEdit() throws Exception {

        Service service = establishConnection();

        EditGeoObjectMessage editMessage = (EditGeoObjectMessage) jsonConverter.stringToMessage(editString);
        service.edit(sessionId, editMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(editString, gisMessage);
    }

    @Test
    public void handleMessageShow() throws Exception {

        Service service = establishConnection();

        ShowGeoObjectMessage showMessage = (ShowGeoObjectMessage) jsonConverter.stringToMessage(showString);
        service.show(sessionId, showMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(showString, gisMessage);
    }

    @Test (expected=ServiceException.class)
    public void sendShowWithoutConnection() throws Exception {

        Service service = new Service(sessionPool, socketSender);

        ShowGeoObjectMessage showMessage = (ShowGeoObjectMessage) jsonConverter.stringToMessage(showString);
        service.show(sessionId, showMessage);

    }

    @Test
    public void useCaseCreateANewObject() throws Exception {

        Service service = establishConnection();

        CreateGeoObjectMessage createMessage = (CreateGeoObjectMessage) jsonConverter.stringToMessage(createString);
        service.create(sessionId, createMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(createString, gisMessage);


        NotifyEditGeoObjectDoneMessage editDoneMessage = (NotifyEditGeoObjectDoneMessage) jsonConverter.stringToMessage(changedString);
        service.changed(sessionId, editDoneMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);

        String appMessage = jsonConverter.messageToString(appMessages.get(1));
        Assert.assertEquals(changedString, appMessage);

        NotifyObjectUpdatedMessage updatedMessage = (NotifyObjectUpdatedMessage) jsonConverter.stringToMessage(dataWrittenString);
        service.dataWritten(sessionId, updatedMessage);

        Assert.assertTrue(gisMessages.size() == 3);

        gisMessage = jsonConverter.messageToString(gisMessages.get(2));
        Assert.assertEquals(dataWrittenString, gisMessage);

    }


    @Test
    public void useCaseEditExistingObject() throws Exception {
        Service service = establishConnection();

        EditGeoObjectMessage editMessage = (EditGeoObjectMessage) jsonConverter.stringToMessage(editString);
        service.edit(sessionId, editMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(editString, gisMessage);


        NotifyEditGeoObjectDoneMessage editDoneMessage = (NotifyEditGeoObjectDoneMessage) jsonConverter.stringToMessage(changedString);
        service.changed(sessionId, editDoneMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);

        String appMessage = jsonConverter.messageToString(appMessages.get(1));
        Assert.assertEquals(changedString, appMessage);

        NotifyObjectUpdatedMessage updatedMessage = (NotifyObjectUpdatedMessage) jsonConverter.stringToMessage(dataWrittenString);
        service.dataWritten(sessionId, updatedMessage);

        Assert.assertTrue(gisMessages.size() == 3);

        gisMessage = jsonConverter.messageToString(gisMessages.get(2));
        Assert.assertEquals(dataWrittenString, gisMessage);
    }

    @Test
    public void useCaseCancelAfterEdit() throws Exception {
        Service service = establishConnection();

        EditGeoObjectMessage editMessage = (EditGeoObjectMessage) jsonConverter.stringToMessage(editString);
        service.edit(sessionId, editMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(editString, gisMessage);

        CancelEditGeoObjectMessage cancelMessage = (CancelEditGeoObjectMessage) jsonConverter.stringToMessage(cancelString);
        service.cancel(sessionId, cancelMessage);

        Assert.assertTrue(gisMessages.size() == 3);

        gisMessage = jsonConverter.messageToString(gisMessages.get(2));
        Assert.assertEquals(cancelString, gisMessage);

    }

    @Test
    public void useCaseCancelAfterCreate() throws Exception {
        Service service = establishConnection();

        CreateGeoObjectMessage createMessage = (CreateGeoObjectMessage) jsonConverter.stringToMessage(createString);
        service.create(sessionId, createMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(createString, gisMessage);

        CancelEditGeoObjectMessage cancelMessage = (CancelEditGeoObjectMessage) jsonConverter.stringToMessage(cancelString);
        service.cancel(sessionId, cancelMessage);

        Assert.assertTrue(gisMessages.size() == 3);

        gisMessage = jsonConverter.messageToString(gisMessages.get(2));
        Assert.assertEquals(cancelString, gisMessage);

    }
}