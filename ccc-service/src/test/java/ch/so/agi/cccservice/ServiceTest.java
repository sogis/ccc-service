package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServiceTest {

    private final String expectedAppName = "App-Name";
    private final String expectedGisName = "GIS-Name";
    private final String sessionString = "{123-456-789-0}";
    private final SessionId sessionId = new SessionId(sessionString);
    private final String session2String = "{123-456-789-2}";
    private final SessionId sessionId2 = new SessionId(session2String);
    private final String apiVersion = "1.0";
    private final String readyString = "{\"method\":\""+NotifySessionReadyMessage.METHOD_NAME+"\",\"apiVersion\":\"1.0\"}";
    private final String createString =
            "{\"method\":\""+CreateGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"zoomTo\":{\"gemeinde\":2542}}";
    private final String editString =
            "{\"method\":\""+EditGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"data\":{\"type\":\"Point\"," +
                    "\"coordinates\":\"[2609190,1226652]\"}}";
    private final String showString = "{\"method\":\""+ShowGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
    private final String cancelString = "{\"method\":\""+CancelEditGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}}";
    private final String changedString = "{\"method\":\""+NotifyEditGeoObjectDoneMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
    private final String selectedString = "{\"method\":\""+NotifyGeoObjectSelectedMessage.METHOD_NAME+"\",\"context_list\":[{\"bfs_num\":231,\"parz_num\":1951}," +
            "{\"bfs_num\":231,\"parz_num\":2634}]}";
    private final String dataWrittenString = "{\"method\":\""+NotifyObjectUpdatedMessage.METHOD_NAME+"\",\"properties\":[\"2017-820\"," +
            "\"Trimbach\"]}";
    private final String errorString = "{\"method\":\""+NotifyErrorMessage.METHOD_NAME+"\",\"code\":999,\"message\":\"test Errormessage\"," +
            "\"userData\":{\"test\":\"3671951\"},\"nativeCode\":\"test nativeCode\"," +
            "\"technicalDetails\":\"test technicalDetails\"}";
    private SocketSenderDummy socketSender = new SocketSenderDummy();
    private JsonConverter jsonConverter = new JsonConverter();

    @After
    public void cleanUpSystemProperties(){
        System.clearProperty(Service.CCC_MAX_INACTIVITY);
        System.clearProperty(Service.CCC_MAX_PAIRING);
    }
    @Test
    public void appConnectMethodTest() throws Exception {

        SessionState sessionState = new SessionState();
        SessionPool sessionPool = new SessionPool();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = createConnectAppMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppMessage(sessionId,appConnectMessage);

        String appName = sessionState.getAppName();

        Assert.assertTrue(sessionState.isAppConnected());
        Assert.assertFalse(sessionState.isGisConnected());
        Assert.assertFalse(sessionState.isReadySent());
        Assert.assertEquals(expectedAppName,appName);

    }

    private ConnectAppMessage createConnectAppMessage(SessionId sessionId, String apiVersion){
        ConnectAppMessage appConnectMessage = new ConnectAppMessage();
        appConnectMessage.setClientName(expectedAppName);
        appConnectMessage.setSession(sessionId);
        appConnectMessage.setApiVersion(apiVersion);

        return appConnectMessage;
    }

    @Test
    public void gisConnectMethodTest() throws Exception {

        SessionState sessionState = new SessionState();
        SessionPool sessionPool = new SessionPool();
        Service service = new Service(sessionPool, socketSender);

        ConnectGisMessage gisConnectMessage = createConnectGisMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleGisMessage(sessionId,gisConnectMessage);

        String gisName = sessionState.getGisName();

        Assert.assertTrue(sessionState.isGisConnected());
        Assert.assertFalse(sessionState.isAppConnected());
        Assert.assertFalse(sessionState.isReadySent());
        Assert.assertEquals(expectedGisName, gisName);
    }

    private ConnectGisMessage createConnectGisMessage(SessionId sessionId, String apiVersion){
        ConnectGisMessage gisConnectMessage = new ConnectGisMessage();
        gisConnectMessage.setClientName(expectedGisName);
        gisConnectMessage.setSession(sessionId);
        gisConnectMessage.setApiVersion(apiVersion);

        return gisConnectMessage;
    }


    @Test
    public void basicConnectionTestGisAfterApp() throws Exception {

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 1);

        String appMessage = jsonConverter.messageToString(appMessages.get(0));
        Assert.assertEquals(appMessage, readyString);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 1);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(0));
        Assert.assertEquals(gisMessage, readyString);
    }

    private Service establishConnection(SessionPool sessionPool) throws Exception{

        SessionState sessionState = new SessionState();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = createConnectAppMessage(sessionId, apiVersion);

        ConnectGisMessage gisConnectMessage = createConnectGisMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppMessage(sessionId,appConnectMessage);
        service.handleGisMessage(sessionId,gisConnectMessage);

        return service;
    }

    @Test
    public void basicConnectionTestAppAfterGis() throws Exception {

        SessionState sessionState = new SessionState();
        SessionPool sessionPool = new SessionPool();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = createConnectAppMessage(sessionId, apiVersion);

        ConnectGisMessage gisConnectMessage = createConnectGisMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleGisMessage(sessionId,gisConnectMessage);
        service.handleAppMessage(sessionId,appConnectMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 1);

        String appMessage = jsonConverter.messageToString(appMessages.get(0));
        Assert.assertEquals(appMessage, readyString);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 1);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(0));
        Assert.assertEquals(gisMessage, readyString);
    }



    @Test
    public void failsWithInactivityTimeOutDetectedBySameSession() throws Exception{
        long maxInactivityTime=20;
        System.setProperty(Service.CCC_MAX_INACTIVITY, Long.toString(maxInactivityTime));
        SessionPool sessionPool = new SessionPool();
        Service service = new Service(sessionPool, socketSender);

        SessionState sessionState = new SessionState();
        sessionPool.addSession(sessionId, sessionState);
        
        ConnectAppMessage appConnectMessage = createConnectAppMessage(sessionId, apiVersion);
        service.handleAppMessage(sessionId,appConnectMessage);

        ConnectGisMessage gisConnectMessage = createConnectGisMessage(sessionId, apiVersion);
        service.handleGisMessage(sessionId,gisConnectMessage);


        TimeUnit.SECONDS.sleep(maxInactivityTime+2);
        ShowGeoObjectMessage showMessage = (ShowGeoObjectMessage) jsonConverter.stringToMessage(showString);
        try {
            service.handleAppMessage(sessionId, showMessage);
            fail();
        }catch(ServiceException e) {
            assertEquals(506,e.getErrorCode());
        }
    }
    
    @Test
    public void failsWithInactivityTimeOutDetectedByOtherSession() throws Exception{
        long maxInactivityTime=20;
        System.setProperty(Service.CCC_MAX_INACTIVITY, Long.toString(maxInactivityTime));
        SessionPool sessionPool = new SessionPool();
        Service service = new Service(sessionPool, socketSender);

        SessionState sessionState = new SessionState();
        sessionPool.addSession(sessionId, sessionState);
        
        ConnectAppMessage appConnectMessage = createConnectAppMessage(sessionId, apiVersion);
        service.handleAppMessage(sessionId,appConnectMessage);

        ConnectGisMessage gisConnectMessage = createConnectGisMessage(sessionId, apiVersion);
        service.handleGisMessage(sessionId,gisConnectMessage);


        TimeUnit.SECONDS.sleep(maxInactivityTime+2);
        
        sessionPool.closeInactiveSessions(maxInactivityTime);
        assertNull(sessionPool.getSession(sessionId));
    }

    @Test (expected = ServiceException.class)
    public void failsWithSessionTimeOut() throws Exception{
        long maxInactivityTime=20;
        System.setProperty(Service.CCC_MAX_PAIRING, Long.toString(maxInactivityTime));

        SessionState sessionState = new SessionState();
        SessionPool sessionPool = new SessionPool();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = createConnectAppMessage(sessionId, apiVersion);

        ConnectGisMessage gisConnectMessage = createConnectGisMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppMessage(sessionId,appConnectMessage);
        TimeUnit.SECONDS.sleep(maxInactivityTime+2);

        service.handleGisMessage(sessionId,gisConnectMessage);
    }

    @Test (expected = ServiceException.class)
    public void failsWithSessionTimeOutOnDoubleAppConnect() throws Exception{

        long maxInactivityTime=20;
        System.setProperty(Service.CCC_MAX_PAIRING, Long.toString(maxInactivityTime));
        SessionState sessionState = new SessionState();
        SessionPool sessionPool = new SessionPool();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = createConnectAppMessage(sessionId, apiVersion);

        ConnectGisMessage gisConnectMessage = createConnectGisMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleGisMessage(sessionId,gisConnectMessage);
        TimeUnit.SECONDS.sleep(maxInactivityTime+2);

        try {
            service.handleAppMessage(sessionId,appConnectMessage);
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
        }

        service.handleAppMessage(sessionId,appConnectMessage);
    }

    @Test (expected = ServiceException.class)
    public void failsWithSessionTimeOutOnDoubleGisConnect() throws Exception{
        long maxInactivityTime=20;
        System.setProperty(Service.CCC_MAX_PAIRING, Long.toString(maxInactivityTime));

        SessionState sessionState = new SessionState();
        SessionPool sessionPool = new SessionPool();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = createConnectAppMessage(sessionId, apiVersion);

        ConnectGisMessage gisConnectMessage = createConnectGisMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppMessage(sessionId,appConnectMessage);
        TimeUnit.SECONDS.sleep(maxInactivityTime+2);

        try {
            service.handleGisMessage(sessionId,gisConnectMessage);
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
        }

        service.handleGisMessage(sessionId,gisConnectMessage);
    }

    @Test
    public void createMethodTest() throws Exception{

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        CreateGeoObjectMessage createMessage = (CreateGeoObjectMessage) jsonConverter.stringToMessage(createString);
        service.handleAppMessage(sessionId, createMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(createString, gisMessage);
    }

    @Test
    public void editMethodTest() throws Exception {

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        EditGeoObjectMessage editMessage = (EditGeoObjectMessage) jsonConverter.stringToMessage(editString);
        service.handleAppMessage(sessionId, editMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(editString, gisMessage);
    }

    @Test
    public void useCaseShow() throws Exception {

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        ShowGeoObjectMessage showMessage = (ShowGeoObjectMessage) jsonConverter.stringToMessage(showString);
        service.handleAppMessage(sessionId, showMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(showString, gisMessage);
    }

    @Test
    public void cancelMethodTest() throws Exception {

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        CancelEditGeoObjectMessage cancelMessage = (CancelEditGeoObjectMessage) jsonConverter.stringToMessage(cancelString);
        service.handleAppMessage(sessionId, cancelMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(cancelString, gisMessage);
    }

    @Test
    public void changedMethodTest() throws Exception{

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        NotifyEditGeoObjectDoneMessage changedMessage = (NotifyEditGeoObjectDoneMessage) jsonConverter.stringToMessage(changedString);
        service.handleGisMessage(sessionId, changedMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(appMessages.get(1));
        Assert.assertEquals(changedString, gisMessage);
    }

    @Test
    public void useCaseSelected() throws Exception{

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        NotifyGeoObjectSelectedMessage selectedMessage = (NotifyGeoObjectSelectedMessage) jsonConverter.stringToMessage(selectedString);
        service.handleGisMessage(sessionId, selectedMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(appMessages.get(1));
        Assert.assertEquals(selectedString, gisMessage);
    }

    @Test
    public void dataWrittenMethodTest() throws Exception {

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        NotifyObjectUpdatedMessage dataWrittenMessage = (NotifyObjectUpdatedMessage) jsonConverter.stringToMessage(dataWrittenString);
        service.handleAppMessage(sessionId, dataWrittenMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(dataWrittenString, gisMessage);
    }

    @Test (expected=ServiceException.class)
    public void failsWithWrongApiVersionOnAppConnect() throws Exception{

        String apiVersion = "2.0";

        SessionState sessionState = new SessionState();
        SessionPool sessionPool = new SessionPool();
        Service service = new Service(sessionPool, socketSender);

        ConnectAppMessage appConnectMessage = createConnectAppMessage(sessionId, apiVersion);
        sessionPool.addSession(sessionId, sessionState);

        service.handleAppMessage(sessionId,appConnectMessage);


    }


    @Test (expected=ServiceException.class)
    public void failsWithWrongApiVersionOnGisConnect() throws Exception{

        String wrongApiVersion= "2.0";

        SessionState sessionState = new SessionState();
        SessionPool sessionPool = new SessionPool();
        Service service = new Service(sessionPool, socketSender);
        ConnectAppMessage appConnectMessage = createConnectAppMessage(sessionId, apiVersion);

        sessionPool.addSession(sessionId, sessionState);

        service.handleAppMessage(sessionId,appConnectMessage);

        ConnectGisMessage gisConnectMessage = createConnectGisMessage(sessionId, wrongApiVersion);

        service.handleGisMessage(sessionId,gisConnectMessage);


    }


    @Test
    public void handleMessageDataWritten() throws Exception{

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        NotifyObjectUpdatedMessage dataWrittenMessage = (NotifyObjectUpdatedMessage) jsonConverter.stringToMessage(dataWrittenString);
        service.handleAppMessage(sessionId, dataWrittenMessage);


        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(dataWrittenString, gisMessage);

    }

    @Test
    public void handleMessageCancel() throws Exception {

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        CancelEditGeoObjectMessage cancelMessage = (CancelEditGeoObjectMessage) jsonConverter.stringToMessage(cancelString);
        service.handleAppMessage(sessionId, cancelMessage);


        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(cancelString, gisMessage);

    }

    @Test
    public void handleMessageSelected() throws Exception{

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        NotifyGeoObjectSelectedMessage selectedMessage = (NotifyGeoObjectSelectedMessage) jsonConverter.stringToMessage(selectedString);
        service.handleGisMessage(sessionId, selectedMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(appMessages.get(1));
        Assert.assertEquals(selectedString, gisMessage);
    }

    @Test
    public void handleMessageChanged() throws Exception{

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        NotifyEditGeoObjectDoneMessage changedMessage = (NotifyEditGeoObjectDoneMessage) jsonConverter.stringToMessage(changedString);
        service.handleGisMessage(sessionId, changedMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(appMessages.get(1));
        Assert.assertEquals(changedString, gisMessage);
    }

    @Test
    public void handleMessageCreate() throws Exception{

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        CreateGeoObjectMessage createMessage = (CreateGeoObjectMessage) jsonConverter.stringToMessage(createString);
        service.handleAppMessage(sessionId, createMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(createString, gisMessage);
    }

    @Test
    public void handleMessageEdit() throws Exception {

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        EditGeoObjectMessage editMessage = (EditGeoObjectMessage) jsonConverter.stringToMessage(editString);
        service.handleAppMessage(sessionId, editMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(editString, gisMessage);
    }

    @Test
    public void handleMessageShow() throws Exception {

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        ShowGeoObjectMessage showMessage = (ShowGeoObjectMessage) jsonConverter.stringToMessage(showString);
        service.handleAppMessage(sessionId, showMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(showString, gisMessage);
    }

    @Test (expected=ServiceException.class)
    public void sendShowWithoutConnection() throws Exception {

        SessionPool sessionPool = new SessionPool();
        Service service = new Service(sessionPool, socketSender);

        ShowGeoObjectMessage showMessage = (ShowGeoObjectMessage) jsonConverter.stringToMessage(showString);
        service.handleAppMessage(sessionId, showMessage);

    }

    @Test
    public void useCaseCreateANewObject() throws Exception {

        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        CreateGeoObjectMessage createMessage = (CreateGeoObjectMessage) jsonConverter.stringToMessage(createString);
        service.handleAppMessage(sessionId, createMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(createString, gisMessage);


        NotifyEditGeoObjectDoneMessage editDoneMessage = (NotifyEditGeoObjectDoneMessage) jsonConverter.stringToMessage(changedString);
        service.handleGisMessage(sessionId, editDoneMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);

        String appMessage = jsonConverter.messageToString(appMessages.get(1));
        Assert.assertEquals(changedString, appMessage);

        NotifyObjectUpdatedMessage updatedMessage = (NotifyObjectUpdatedMessage) jsonConverter.stringToMessage(dataWrittenString);
        service.handleAppMessage(sessionId, updatedMessage);

        Assert.assertTrue(gisMessages.size() == 3);

        gisMessage = jsonConverter.messageToString(gisMessages.get(2));
        Assert.assertEquals(dataWrittenString, gisMessage);

    }


    @Test
    public void useCaseEditExistingObject() throws Exception {
        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        EditGeoObjectMessage editMessage = (EditGeoObjectMessage) jsonConverter.stringToMessage(editString);
        service.handleAppMessage(sessionId, editMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(editString, gisMessage);


        NotifyEditGeoObjectDoneMessage editDoneMessage = (NotifyEditGeoObjectDoneMessage) jsonConverter.stringToMessage(changedString);
        service.handleGisMessage(sessionId, editDoneMessage);

        List<AbstractMessage> appMessages = socketSender.getAppMessages();
        Assert.assertTrue(appMessages.size() == 2);

        String appMessage = jsonConverter.messageToString(appMessages.get(1));
        Assert.assertEquals(changedString, appMessage);

        NotifyObjectUpdatedMessage updatedMessage = (NotifyObjectUpdatedMessage) jsonConverter.stringToMessage(dataWrittenString);
        service.handleAppMessage(sessionId, updatedMessage);

        Assert.assertTrue(gisMessages.size() == 3);

        gisMessage = jsonConverter.messageToString(gisMessages.get(2));
        Assert.assertEquals(dataWrittenString, gisMessage);
    }

    @Test
    public void useCaseCancelAfterEdit() throws Exception {
        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        EditGeoObjectMessage editMessage = (EditGeoObjectMessage) jsonConverter.stringToMessage(editString);
        service.handleAppMessage(sessionId, editMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(editString, gisMessage);

        CancelEditGeoObjectMessage cancelMessage = (CancelEditGeoObjectMessage) jsonConverter.stringToMessage(cancelString);
        service.handleAppMessage(sessionId, cancelMessage);

        Assert.assertTrue(gisMessages.size() == 3);

        gisMessage = jsonConverter.messageToString(gisMessages.get(2));
        Assert.assertEquals(cancelString, gisMessage);

    }

    @Test
    public void useCaseCancelAfterCreate() throws Exception {
        SessionPool sessionPool = new SessionPool();
        Service service = establishConnection(sessionPool);

        CreateGeoObjectMessage createMessage = (CreateGeoObjectMessage) jsonConverter.stringToMessage(createString);
        service.handleAppMessage(sessionId, createMessage);

        List<AbstractMessage> gisMessages = socketSender.getGisMessages();
        Assert.assertTrue(gisMessages.size() == 2);

        String gisMessage = jsonConverter.messageToString(gisMessages.get(1));
        Assert.assertEquals(createString, gisMessage);

        CancelEditGeoObjectMessage cancelMessage = (CancelEditGeoObjectMessage) jsonConverter.stringToMessage(cancelString);
        service.handleAppMessage(sessionId, cancelMessage);

        Assert.assertTrue(gisMessages.size() == 3);

        gisMessage = jsonConverter.messageToString(gisMessages.get(2));
        Assert.assertEquals(cancelString, gisMessage);

    }
}