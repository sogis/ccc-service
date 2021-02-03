package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class JsonConverterTest {
    //private String appConnectString = "{\"method\":\""+ConnectAppMessage.METHOD_NAME+"\"," +
    private String connectAppString = "{\"method\":\""+ConnectAppMessage.METHOD_NAME+"\"," +
            "\"session\":\"{235ea7d3-8069-4bbc-b7de-17ff15239e7c}\"," +
            "\"clientName\":\"Axioma Mandant AfU\"," +
            "\"apiVersion\":\"1.0\"}";
    private String connectGisString = "{\"method\":\""+ConnectGisMessage.METHOD_NAME+"\"," +
            "\"session\":\"{235ea7d3-8069-4bbc-b7de-17ff15239e7c}\"," +
            "\"clientName\":\"Web GIS Client\"," +
            "\"apiVersion\":\"1.0\"}";
    private String cancelEditGeoObjectString = "{\"method\":\""+CancelEditGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}}";
    private String notifyEditGeoObjectDoneMessageString = "{\"method\":\""+NotifyEditGeoObjectDoneMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
    private String createGeoObjectString = "{\"method\":\""+CreateGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"zoomTo\":{\"gemeinde\":2542}}";
    private String notifyObjectUpdatedString = "{\"method\":\""+NotifyObjectUpdatedMessage.METHOD_NAME+"\",\"properties\":[\"2017-820\"," +
            "\"Trimbach\"]}";
    private String editGeoObjectString = "{\"method\":\""+EditGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
    private String notifyErrorString = "{\"method\":\""+NotifyErrorMessage.METHOD_NAME+"\",\"code\":999,\"message\":\"test Errormessage\"," +
            "\"userData\":{\"test\":\"3671951\"},\"nativeCode\":\"test nativeCode\"," +
            "\"technicalDetails\":\"test technicalDetails\"}";
    private String notifyGeoObjectSelectedString = "{\"method\":\""+NotifyGeoObjectSelectedMessage.METHOD_NAME+"\",\"context_list\":[{\"bfs_num\":231,\"parz_num\":1951}," +
            "{\"bfs_num\":231,\"parz_num\":2634}]}";
    private String notifySessionReadyString = "{\"method\":\""+NotifySessionReadyMessage.METHOD_NAME+"\",\"apiVersion\":\"1.0\"}";
    private String showGeoObjectString = "{\"method\":\""+ShowGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";

    private JsonConverter jsonConverter = new JsonConverter();

    @Test
    public void connectAppMessageToString() throws IOException {
        ConnectAppMessage appConnectMessage = new ConnectAppMessage();
        appConnectMessage.setApiVersion("1.0");
        appConnectMessage.setSession(new SessionId("{235ea7d3-8069-4bbc-b7de-17ff15239e7c}"));
        appConnectMessage.setClientName("Axioma Mandant AfU");

        String resultingJson = jsonConverter.messageToString(appConnectMessage);

        assertEquals(connectAppString,resultingJson);

    }

    @Test
    public void stringToConnectAppMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(connectAppString) instanceof ConnectAppMessage);
    }


    @Test
    public void connectGisMessageToString() throws IOException {
        ConnectGisMessage gisConnectMessage = new ConnectGisMessage();
        gisConnectMessage.setApiVersion("1.0");
        gisConnectMessage.setSession(new SessionId("{235ea7d3-8069-4bbc-b7de-17ff15239e7c}"));
        gisConnectMessage.setClientName("Web GIS Client");

        String resultingJson = jsonConverter.messageToString(gisConnectMessage);

        assertEquals(connectGisString,resultingJson);
    }

    @Test
    public void stringToConnectGisMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(connectGisString) instanceof ConnectGisMessage);
    }

    @Test
    public void cancelEditGeoObjectMessageToString() throws IOException {
        CancelEditGeoObjectMessage cancelMessage = new CancelEditGeoObjectMessage();
        cancelMessage.setContext(createContextNode());
        String resultingJson = jsonConverter.messageToString(cancelMessage);
        assertTrue(resultingJson.equals(cancelEditGeoObjectString));
    }

    @Test
    public void stringToCancelEditGeoObjectMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(cancelEditGeoObjectString) instanceof CancelEditGeoObjectMessage);
    }

    @Test
    public void notifyEditGeoObjectDoneMessageToString() throws IOException {
        NotifyEditGeoObjectDoneMessage changedMessage = new NotifyEditGeoObjectDoneMessage();
        changedMessage.setData(createDataNode());
        changedMessage.setContext(createContextNode());
        String resultingJson = jsonConverter.messageToString(changedMessage);
        assertTrue(resultingJson.equals(notifyEditGeoObjectDoneMessageString));
    }

    @Test
    public void stringToNotifyEditGeoObjectDoneMessage() throws IOException, ServiceException {
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(notifyEditGeoObjectDoneMessageString) instanceof NotifyEditGeoObjectDoneMessage);
    }
    
    @Test
    public void stringToNotifyEditGeoObjectDoneMessageDataNull() throws IOException, ServiceException {
        final String notifyEditGeoObjectDoneMessageString = "{\"method\":\""+NotifyEditGeoObjectDoneMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
                "\"data\":null}";
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(notifyEditGeoObjectDoneMessageString) instanceof NotifyEditGeoObjectDoneMessage);
    }
    @Test
    public void stringToNotifyEditGeoObjectDoneMessageDataMissing() throws IOException, ServiceException {
        final String notifyEditGeoObjectDoneMessageString = "{\"method\":\""+NotifyEditGeoObjectDoneMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}" +
                "}";
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(notifyEditGeoObjectDoneMessageString) instanceof NotifyEditGeoObjectDoneMessage);
    }


    @Test
    public void createGeoObjectMessageToString() throws IOException {
        CreateGeoObjectMessage createMessage = new CreateGeoObjectMessage();
        createMessage.setZoomTo(createZoomToNode());
        createMessage.setContext(createContextNode());
        String resultingJson = jsonConverter.messageToString(createMessage);
        assertTrue(resultingJson.equals(createGeoObjectString));
    }

    @Test
    public void stringToCreateGeoObjectMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(createGeoObjectString) instanceof CreateGeoObjectMessage);
    }
    @Test
    public void stringToCreateGeoObjectMessageZoomToNull() throws IOException, ServiceException {
        final String createGeoObjectString = "{\"method\":\""+CreateGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
                "\"zoomTo\":null}";
        assertTrue(jsonConverter.stringToMessage(createGeoObjectString) instanceof CreateGeoObjectMessage);
    }

    @Test
    public void notifyObjectUpdatedMessageToString() throws IOException {
        NotifyObjectUpdatedMessage dataWrittenMessage = new NotifyObjectUpdatedMessage();
        dataWrittenMessage.setProperties(createPropertiesNode());
        String resultingJson = jsonConverter.messageToString(dataWrittenMessage);
        assertTrue(resultingJson.equals(notifyObjectUpdatedString));
    }

    @Test
    public void stringWithArrayToNotifyObjectUpdatedMessage() throws IOException, ServiceException {
        String dataWrittenString = "{\"method\":\""+NotifyObjectUpdatedMessage.METHOD_NAME+"\",\"properties\":[{\"laufnr\":\"2017-820\"," +
                "\"grundbuch\":\"Trimbach\"}]}";
        assertTrue(jsonConverter.stringToMessage(dataWrittenString) instanceof NotifyObjectUpdatedMessage);
    }

    @Test
    public void editGeoObjectMessageToString() throws IOException {
        EditGeoObjectMessage editMessage = new EditGeoObjectMessage();
        editMessage.setData(createDataNode());
        editMessage.setContext(createContextNode());
        String resultingJson = jsonConverter.messageToString(editMessage);
        assertTrue(resultingJson.equals(editGeoObjectString));
    }

    @Test
    public void stringToEditGeoObjectMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(editGeoObjectString) instanceof EditGeoObjectMessage);
    }

    @Test
    public void notifyErrorMessageToString() throws IOException {
        NotifyErrorMessage errorMessage = new NotifyErrorMessage();
        errorMessage.setCode(999);
        errorMessage.setMessage("test Errormessage");
        errorMessage.setUserData(createUserDataToNode());
        errorMessage.setNativeCode("test nativeCode");
        errorMessage.setTechnicalDetails("test technicalDetails");
        String resultingJson = jsonConverter.messageToString(errorMessage);
        assertTrue(resultingJson.equals(notifyErrorString));

    }

    @Test
    public void stringToNotifyErrorMessage () throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(notifyErrorString) instanceof NotifyErrorMessage);
    }

    @Test
    public void notifySessionReadyMessageToString() throws IOException {
        NotifySessionReadyMessage readyMessage = new NotifySessionReadyMessage();
        readyMessage.setApiVersion("1.0");
        String resultingJson = jsonConverter.messageToString(readyMessage);
        assertTrue(resultingJson.equals(notifySessionReadyString));
    }

    @Test
    public void stringToNotifySessionReadyMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(notifySessionReadyString) instanceof NotifySessionReadyMessage);
    }

    @Test
    public void notifyGeoObjectSelectedMessageToString() throws IOException {
        NotifyGeoObjectSelectedMessage selectedMessage = new NotifyGeoObjectSelectedMessage();
        selectedMessage.setContext_list(createContextListNode());
        String resultingJson = jsonConverter.messageToString(selectedMessage);
        assertTrue(resultingJson.equals(notifyGeoObjectSelectedString));
    }

    @Test
    public void stringToNotifyGeoObjectSelectedMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(notifyGeoObjectSelectedString) instanceof NotifyGeoObjectSelectedMessage);
    }
    @Test
    public void stringToNotifyGeoObjectSelectedMessageContext_listNull() throws IOException, ServiceException {
        final String notifyGeoObjectSelectedString = "{\"method\":\""+NotifyGeoObjectSelectedMessage.METHOD_NAME+"\",\"context_list\":null}";
        assertTrue(jsonConverter.stringToMessage(notifyGeoObjectSelectedString) instanceof NotifyGeoObjectSelectedMessage);
    }

    @Test
    public void showGeoObjectMessageToString() throws IOException {
        ShowGeoObjectMessage showMessage = new ShowGeoObjectMessage();
        showMessage.setData(createDataNode());
        showMessage.setContext(createContextNode());
        String resultingJson = jsonConverter.messageToString(showMessage);
        assertTrue(resultingJson.equals(showGeoObjectString));
    }

    @Test
    public void stringToShowGeoObjectMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(showGeoObjectString) instanceof ShowGeoObjectMessage);
    }


    @Test (expected = JsonEOFException.class)
    public void stringToCorruptJsonEofTest() throws IOException, ServiceException {
        String json = "{\"method\":\"appConnect\"," +
                "\"session\":{\"sessionId\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"}," +
                "\"clientName\":\"Axioma Mandant AfU\"," +
                "\"apiVersion\":\"1.0\""; //Es fehlt die letzte Klammer }
        jsonConverter.stringToMessage(json);
    }

    @Test (expected = ServiceException.class)
    public void stringToNoMethodTest() throws IOException, ServiceException {
        String json = "{" + //Keine Methode
                "\"session\":{\"sessionId\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"}," +
                "\"clientName\":\"Axioma Mandant AfU\"," +
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(json);
    }

    @Test (expected = JsonParseException.class)
    public void stringToCorruptJsonTest() throws IOException, ServiceException {
        String json = "{\"method\":\"appConnect\"," +
                "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
                "\"clientNameAxioma Mandant AfU\"," + //Hier ist doch was faul...
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(json);
    }

    @Test (expected = ServiceException.class)
    public void stringToConnectAppMessageMissingJsonProperty() throws IOException, ServiceException {
        String json = "{\"method\":\""+ConnectAppMessage.METHOD_NAME+"\"," +
                "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
                //missing client name
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(json);
    }

    @Test (expected = ServiceException.class)
    public void stringToNotifyEditGeoObjectDoneMessageWrongNameOfContextAttribute() throws IOException, ServiceException {
        String messageWithMissingCode = "{\"method\":\""+NotifyEditGeoObjectDoneMessage.METHOD_NAME+"\",\"contect\":{\"afu_geschaeft\":\"3671951\"}," +
                "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
        jsonConverter.stringToMessage(messageWithMissingCode);
    }

    @Test (expected = ServiceException.class)
    public void stringToConnectAppMessageMissingApiVersion() throws IOException, ServiceException {
        String appConnectMessageWithoutApiVersion = "{\"method\":\""+ConnectAppMessage.METHOD_NAME+"\"," +
                "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
                "\"clientName\":\"Axioma Mandant AfU\"}";
        jsonConverter.stringToMessage(appConnectMessageWithoutApiVersion);
    }

    @Test (expected = ServiceException.class)
    public void stringToConnectAppMessageMissingSession() throws IOException, ServiceException {
        String appConnectMessageWithoutSession = "{\"method\":\""+ConnectAppMessage.METHOD_NAME+"\"," +
                "\"clientName\":\"Axioma Mandant AfU\"," +
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(appConnectMessageWithoutSession);
    }

    @Test (expected = ServiceException.class)
    public void stringToConnectAppMessageMissingClientName() throws IOException, ServiceException {
        String appConnectMessageWithoutClientName = "{\"method\":\""+ConnectAppMessage.METHOD_NAME+"\"," +
                "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(appConnectMessageWithoutClientName);
    }

    @Test (expected = ServiceException.class)
    public void stringToConnectGisMessageMissingApiVersion() throws IOException, ServiceException {
        String gisConnectMessageWithoutApiVersion = "{\"method\":\""+ConnectGisMessage.METHOD_NAME+"\"," +
                "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
                "\"clientName\":\"Axioma Mandant AfU\"}";
        jsonConverter.stringToMessage(gisConnectMessageWithoutApiVersion);
    }

    @Test (expected = ServiceException.class)
    public void stringToConnectGisMessageMissingSession() throws IOException, ServiceException {
        String gisConnectMessageWithoutSession = "{\"method\":\""+ConnectGisMessage.METHOD_NAME+"\"," +
                "\"clientName\":\"Axioma Mandant AfU\"," +
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(gisConnectMessageWithoutSession);
    }

    @Test (expected = ServiceException.class)
    public void stringToConnectGisMessageMissingClientName() throws IOException, ServiceException {
        String gisConnectMessageWithoutClientName = "{\"method\":\""+ConnectGisMessage.METHOD_NAME+"\"," +
                "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(gisConnectMessageWithoutClientName);
    }

    @Test (expected = ServiceException.class)
    public void stringToCreateGeoObjectMessageMissingContext() throws IOException, ServiceException {
        String createMessageWithoutContext = "{\"method\":\""+CreateGeoObjectMessage.METHOD_NAME+"\"," +
                "\"zoomTo\":{\"gemeinde\":2542}}";
        jsonConverter.stringToMessage(createMessageWithoutContext);
    }

    @Test (expected = ServiceException.class)
    public void stringToCreateGeoObjectMessageWrongTypeOnZoomTo() throws IOException, ServiceException {
        String createString = "{\"method\":\""+CreateGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
                "\"zoomTo\":\"gemeinde: 2542\"}";
        jsonConverter.stringToMessage(createString);
    }

    @Test (expected = ServiceException.class)
    public void stringToEditGeoObjectMessageMissingContext() throws IOException, ServiceException {
        String editMessageWithoutContext = "{\"method\":\""+EditGeoObjectMessage.METHOD_NAME+"\"," +
                "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
        jsonConverter.stringToMessage(editMessageWithoutContext);
    }

    @Test (expected = ServiceException.class)
    public void stringToEditGeoObjectMessageMissingData() throws IOException, ServiceException {
        String editMessageWithoutData = "{\"method\":\""+EditGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}}";
        jsonConverter.stringToMessage(editMessageWithoutData);
    }

    @Test (expected = ServiceException.class)
    public void stringToEditGeoObjectMessageWrongTypeOnData() throws IOException, ServiceException {
        String editString = "{\"method\":\""+EditGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
                "\"data\":\"{type:Point,coordinates:[2609190,1226652]}\"}";
        jsonConverter.stringToMessage(editString);
    }

    @Test (expected = ServiceException.class)
    public void stringToShowGeoObjectMessageMissingContext() throws IOException, ServiceException {
        String showMessageWithoutContext = "{\"method\":\""+ShowGeoObjectMessage.METHOD_NAME+"\"," +
                "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
        jsonConverter.stringToMessage(showMessageWithoutContext);
    }

    @Test (expected = ServiceException.class)
    public void stringToShowGeoObjectMessageMissingData() throws IOException, ServiceException {
        String showMessageWithoutData = "{\"method\":\""+ShowGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}}";
        jsonConverter.stringToMessage(showMessageWithoutData);
    }

    @Test (expected = ServiceException.class)
    public void stringToShowGeoObjectMessageWrongTypeOnData() throws IOException, ServiceException {
        String showString = "{\"method\":\""+ShowGeoObjectMessage.METHOD_NAME+"\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
                "\"data\":\"{type:Point,coordinates:[2609190,1226652]}\"}";
        jsonConverter.stringToMessage(showString);
    }

    @Test (expected = ServiceException.class)
    public void stringToCancelEditGeoObjectMessageMissingContext() throws IOException, ServiceException {
        String cancelMessageWithoutContext = "{\"method\":\""+CancelEditGeoObjectMessage.METHOD_NAME+"\"}";
        jsonConverter.stringToMessage(cancelMessageWithoutContext);
    }

    @Test (expected = ServiceException.class)
    public void stringToCancelEditGeoObjectMessageWrongTypeOnContext() throws IOException, ServiceException {
        String cancelString = "{\"method\":\""+CancelEditGeoObjectMessage.METHOD_NAME+"\",\"context\":\"{afu_geschaeft:3671951}\"}";
        jsonConverter.stringToMessage(cancelString);
    }

    @Test (expected = ServiceException.class)
    public void stringToNotifyEditGeoObjectDoneMessageMissingContextAttribute() throws IOException, ServiceException {
        String messageWithMissingContext = "{\"method\":\""+NotifyEditGeoObjectDoneMessage.METHOD_NAME+"\"," +
                "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
        jsonConverter.stringToMessage(messageWithMissingContext);
    }

    @Test (expected = ServiceException.class)
    public void stringToNotifyObjectUpdatedMessageMissingProperties() throws IOException, ServiceException {
        String dataWrittenMessageWithoutProperties = "{\"method\":\""+NotifyObjectUpdatedMessage.METHOD_NAME+"\"}";
        jsonConverter.stringToMessage(dataWrittenMessageWithoutProperties);
    }
    @Test (expected = ServiceException.class)
    public void stringToNotifyObjectUpdatedMessageWrongObjectTypeOnProperties() throws IOException, ServiceException {
        String message = "{\"method\":\""+NotifyObjectUpdatedMessage.METHOD_NAME+"\",\"properties\":{\"laufnr\":\"2017-820\"," +
                "\"grundbuch\":\"Trimbach\"}}";
        assertTrue(jsonConverter.stringToMessage(message) instanceof NotifyObjectUpdatedMessage);
    }
    @Test (expected = ServiceException.class)
    public void stringToNotifyObjectUpdatedMessageWrongStringTypeOnProperties() throws IOException, ServiceException {
        String dataWrittenString = "{\"method\":\""+NotifyObjectUpdatedMessage.METHOD_NAME+"\",\"properties\":\"{laufnr:2017-820," +
                "grundbuch:Trimbach}\"}";
        jsonConverter.stringToMessage(dataWrittenString);
    }

    @Test (expected = ServiceException.class)
    public void stringToNotifyGeoObjectSelectedMessageWrongTypeOnContext_list() throws IOException, ServiceException {
        String selectedString = "{\"method\":\""+NotifyGeoObjectSelectedMessage.METHOD_NAME+"\",\"context_list\":" +
                "\"[{bfs_num:231,parz_num:1951},{bfs_num:231,parz_num:2634}]\"}";
        jsonConverter.stringToMessage(selectedString);
    }

    @Test (expected = ServiceException.class)
    public void stringToNotifyErrorMessageMissingCodeAttribute() throws IOException, ServiceException {
        String errorMessageWithoutCode = "{\"method\":\""+NotifyErrorMessage.METHOD_NAME+"\",\"message\":\"test Errormessage\"," +
                "\"userData\":{\"test\":\"3671951\"},\"nativeCode\":\"test nativeCode\"," +
                "\"technicalDetails\":\"test technicalDetails\"}";
        jsonConverter.stringToMessage(errorMessageWithoutCode);
    }

    @Test (expected = ServiceException.class)
    public void stringToNotifyErrorMessageMissingMessageAttribute() throws IOException, ServiceException {
        String errorMessageWithoutMessage = "{\"method\":\""+NotifyErrorMessage.METHOD_NAME+"\",\"code\":999}";
        jsonConverter.stringToMessage(errorMessageWithoutMessage);
    }

    @Test (expected = ServiceException.class)
    public void stringToNotifyErrorMessageWrongTypOnUserData() throws IOException, ServiceException {
        String errorString = "{\"method\":\""+NotifyErrorMessage.METHOD_NAME+"\",\"code\":999,\"message\":\"test Errormessage\"," +
                "\"userData\":\"{test:3671951}\",\"nativeCode\":\"test nativeCode\"," +
                "\"technicalDetails\":\"test technicalDetails\"}";
        jsonConverter.stringToMessage(errorString);

    }


    //TEST-HILFSKLASSEN

    private JsonNode createNode (String str) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(str);
    }
    private JsonNode createDataNode () throws IOException {
        return createNode("{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}");
    }
    private JsonNode createContextNode () throws IOException {
        return createNode("{\"afu_geschaeft\":\"3671951\"}");
    }
    private JsonNode createZoomToNode () throws IOException {
        return createNode("{\"gemeinde\":2542}");
    }
    private JsonNode createPropertiesNode () throws IOException {
        return createNode("[\"2017-820\",\"Trimbach\"]");
    }
    private JsonNode createContextListNode () throws IOException {
        return createNode("[{\"bfs_num\":231,\"parz_num\":1951},{\"bfs_num\":231,\"parz_num\":2634}]");
    }

    private JsonNode createUserDataToNode() throws IOException {
        return createNode("{\"test\":\"3671951\"}");
    }
}