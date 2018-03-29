package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class JsonConverterTest {
    private String appConnectString = "{\"method\":\"appConnect\"," +
            "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
            "\"clientName\":\"Axioma Mandant AfU\"," +
            "\"apiVersion\":\"1.0\"}";
    private String gisConnectString = "{\"method\":\"gisConnect\"," +
            "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
            "\"clientName\":\"Web GIS Client\"," +
            "\"apiVersion\":\"1.0\"}";
    private String cancelString = "{\"method\":\"cancel\",\"context\":{\"afu_geschaeft\":\"3671951\"}}";
    private String changedString = "{\"method\":\"changed\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
    private String createString = "{\"method\":\"create\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"zoomTo\":{\"gemeinde\":2542}}";
    private String dataWrittenString = "{\"method\":\"dataWritten\",\"properties\":{\"laufnr\":\"2017-820\"," +
            "\"grundbuch\":\"Trimbach\"}}";
    private String editString = "{\"method\":\"edit\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
    private String errorString = "{\"method\":\"error\",\"code\":999,\"message\":\"test Errormessage\"," +
            "\"userData\":{\"test\":\"3671951\"},\"nativeCode\":\"test nativeCode\"," +
            "\"technicalDetails\":\"test technicalDetails\"}";
    private String selectedString = "{\"method\":\"selected\",\"context_list\":[{\"bfs_num\":231,\"parz_num\":1951}," +
            "{\"bfs_num\":231,\"parz_num\":2634}]}";
    private String readyString = "{\"method\":\"ready\",\"apiVersion\":\"1.0\"}";
    private String showString = "{\"method\":\"show\",\"context\":{\"afu_geschaeft\":\"3671951\"}," +
            "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";

    private JsonConverter jsonConverter = new JsonConverter();

    @Test
    public void appConnectMessageToString() throws IOException {
        AppConnectMessage appConnectMessage = new AppConnectMessage();
        appConnectMessage.setApiVersion("1.0");
        appConnectMessage.setSession(new SessionId("{E9-TRALLALLA-UND-BLA-BLA-BLA-666}"));
        appConnectMessage.setClientName("Axioma Mandant AfU");

        String resultingJson = jsonConverter.messageToString(appConnectMessage);

        assertTrue(resultingJson.equals(appConnectString));

    }

    @Test
    public void stringToAppConnectMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(appConnectString) instanceof AppConnectMessage);
    }


    @Test
    public void gisConnectMessageToString() throws IOException {
        GisConnectMessage gisConnectMessage = new GisConnectMessage();
        gisConnectMessage.setApiVersion("1.0");
        gisConnectMessage.setSession(new SessionId("{E9-TRALLALLA-UND-BLA-BLA-BLA-666}"));
        gisConnectMessage.setClientName("Web GIS Client");

        String resultingJson = jsonConverter.messageToString(gisConnectMessage);

        assertTrue(resultingJson.equals(gisConnectString));
    }

    @Test
    public void stringToGisConnectMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(gisConnectString) instanceof GisConnectMessage);
    }

    @Test
    public void cancelMessageToString() throws IOException {
        CancelMessage cancelMessage = new CancelMessage();
        cancelMessage.setContext(createContextNode());
        String resultingJson = jsonConverter.messageToString(cancelMessage);
        assertTrue(resultingJson.equals(cancelString));
    }

    @Test
    public void stringToCancelMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(cancelString) instanceof CancelMessage);
    }

    @Test
    public void changedMessageToString() throws IOException {
        ChangedMessage changedMessage = new ChangedMessage();
        changedMessage.setData(createDataNode());
        changedMessage.setContext(createContextNode());
        String resultingJson = jsonConverter.messageToString(changedMessage);
        assertTrue(resultingJson.equals(changedString));
    }

    @Test
    public void stringToChangedMessage() throws IOException, ServiceException {
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(changedString) instanceof ChangedMessage);
    }

    @Test
    public void createMessageToString() throws IOException {
        CreateMessage createMessage = new CreateMessage();
        createMessage.setZoomTo(createZoomToNode());
        createMessage.setContext(createContextNode());
        String resultingJson = jsonConverter.messageToString(createMessage);
        assertTrue(resultingJson.equals(createString));
    }

    @Test
    public void stringToCreateMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(createString) instanceof CreateMessage);
    }

    @Test
    public void dataWrittenToString() throws IOException {
        DataWrittenMessage dataWrittenMessage = new DataWrittenMessage();
        dataWrittenMessage.setProperties(createPropertiesNode());
        String resultingJson = jsonConverter.messageToString(dataWrittenMessage);
        assertTrue(resultingJson.equals(dataWrittenString));
    }

    @Test
    public void stringToDataWrittenMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(dataWrittenString) instanceof DataWrittenMessage);
    }

    @Test
    public void editMessageToString() throws IOException {
        EditMessage editMessage = new EditMessage();
        editMessage.setData(createDataNode());
        editMessage.setContext(createContextNode());
        String resultingJson = jsonConverter.messageToString(editMessage);
        assertTrue(resultingJson.equals(editString));
    }

    @Test
    public void stringToEditMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(editString) instanceof EditMessage);
    }

    @Test
    public void errorMessageToString() throws IOException {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setCode(999);
        errorMessage.setMessage("test Errormessage");
        errorMessage.setUserData(createUserDataToNode());
        errorMessage.setNativeCode("test nativeCode");
        errorMessage.setTechnicalDetails("test technicalDetails");
        String resultingJson = jsonConverter.messageToString(errorMessage);
        assertTrue(resultingJson.equals(errorString));

    }

    @Test
    public void stringToErrorMessage () throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(errorString) instanceof ErrorMessage);
    }

    @Test
    public void readyMessageToString() throws IOException {
        ReadyMessage readyMessage = new ReadyMessage();
        readyMessage.setApiVersion("1.0");
        String resultingJson = jsonConverter.messageToString(readyMessage);
        assertTrue(resultingJson.equals(readyString));
    }

    @Test
    public void stringToReadyMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(readyString) instanceof ReadyMessage);
    }

    @Test
    public void selectedMessageToString() throws IOException {
        SelectedMessage selectedMessage = new SelectedMessage();
        selectedMessage.setContext_list(createContextListNode());
        String resultingJson = jsonConverter.messageToString(selectedMessage);
        assertTrue(resultingJson.equals(selectedString));
    }

    @Test
    public void stringToSelectedMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(selectedString) instanceof SelectedMessage);
    }

    @Test
    public void showMessageToString() throws IOException {
        ShowMessage showMessage = new ShowMessage();
        showMessage.setData(createDataNode());
        showMessage.setContext(createContextNode());
        String resultingJson = jsonConverter.messageToString(showMessage);
        assertTrue(resultingJson.equals(showString));
    }

    @Test
    public void stringToShowMessage() throws IOException, ServiceException {
        assertTrue(jsonConverter.stringToMessage(showString) instanceof ShowMessage);
    }


    @Test (expected = JsonEOFException.class)
    public void corruptJsonEofTest() throws IOException, ServiceException {
        String json = "{\"method\":\"appConnect\"," +
                "\"session\":{\"sessionId\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"}," +
                "\"clientName\":\"Axioma Mandant AfU\"," +
                "\"apiVersion\":\"1.0\""; //Es fehlt die letzte Klammer }
        jsonConverter.stringToMessage(json);
    }

    @Test (expected = ServiceException.class)
    public void noMethodTest() throws IOException, ServiceException {
        String json = "{" + //Keine Methode
                "\"session\":{\"sessionId\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"}," +
                "\"clientName\":\"Axioma Mandant AfU\"," +
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(json);
    }

    @Test (expected = JsonParseException.class)
    public void corruptJsonTest() throws IOException, ServiceException {
        String json = "{\"method\":\"appConnect\"," +
                "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
                "\"clientNameAxioma Mandant AfU\"," + //Hier ist doch was faul...
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(json);
    }

    @Test (expected = ServiceException.class)
    public void missingJsonProperty() throws IOException, ServiceException {
        String json = "{\"method\":\"appConnect\"," +
                "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
                //missing client name
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(json);
    }

    @Test (expected = ServiceException.class)
    public void wrongNameOfContextAttributeInErrorMessage() throws IOException, ServiceException {
        String messageWithMissingCode = "{\"method\":\"changed\",\"contect\":{\"afu_geschaeft\":\"3671951\"}," +
                "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
        jsonConverter.stringToMessage(messageWithMissingCode);
    }

    @Test (expected = ServiceException.class)
    public void missingApiVersionInAppConnectMessage() throws IOException, ServiceException {
        String appConnectMessageWithoutApiVersion = "{\"method\":\"appConnect\"," +
                "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
                "\"clientName\":\"Axioma Mandant AfU\"}";
        jsonConverter.stringToMessage(appConnectMessageWithoutApiVersion);
    }

    @Test (expected = ServiceException.class)
    public void missingSessionInAppConnectMessage() throws IOException, ServiceException {
        String appConnectMessageWithoutSession = "{\"method\":\"appConnect\"," +
                "\"clientName\":\"Axioma Mandant AfU\"," +
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(appConnectMessageWithoutSession);
    }

    @Test (expected = ServiceException.class)
    public void missingClientNameInAppConnectMessage() throws IOException, ServiceException {
        String appConnectMessageWithoutClientName = "{\"method\":\"appConnect\"," +
                "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(appConnectMessageWithoutClientName);
    }

    @Test (expected = ServiceException.class)
    public void missingApiVersionInGisConnectMessage() throws IOException, ServiceException {
        String gisConnectMessageWithoutApiVersion = "{\"method\":\"gisConnect\"," +
                "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
                "\"clientName\":\"Axioma Mandant AfU\"}";
        jsonConverter.stringToMessage(gisConnectMessageWithoutApiVersion);
    }

    @Test (expected = ServiceException.class)
    public void missingSessionInGisConnectMessage() throws IOException, ServiceException {
        String gisConnectMessageWithoutSession = "{\"method\":\"gisConnect\"," +
                "\"clientName\":\"Axioma Mandant AfU\"," +
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(gisConnectMessageWithoutSession);
    }

    @Test (expected = ServiceException.class)
    public void missingClientNameInGisConnectMessage() throws IOException, ServiceException {
        String gisConnectMessageWithoutClientName = "{\"method\":\"gisConnect\"," +
                "\"session\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"," +
                "\"apiVersion\":\"1.0\"}";
        jsonConverter.stringToMessage(gisConnectMessageWithoutClientName);
    }

    @Test (expected = ServiceException.class)
    public void missingContextInCreateMessage() throws IOException, ServiceException {
        String createMessageWithoutContext = "{\"method\":\"create\"," +
                "\"zoomTo\":{\"gemeinde\":2542}}";
        jsonConverter.stringToMessage(createMessageWithoutContext);
    }

    /*todo: Abklären, ob getestet werden soll, ob im ZoomTo-Attribut ein Objekt abgespeichert ist.*/

    @Test (expected = ServiceException.class)
    public void missingContextInEditMessage() throws IOException, ServiceException {
        String editMessageWithoutContext = "{\"method\":\"edit\"," +
                "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
        jsonConverter.stringToMessage(editMessageWithoutContext);
    }

    @Test (expected = ServiceException.class)
    public void missingDataInEditMessage() throws IOException, ServiceException {
        String editMessageWithoutData = "{\"method\":\"edit\",\"context\":{\"afu_geschaeft\":\"3671951\"}}";
        jsonConverter.stringToMessage(editMessageWithoutData);
    }
    /*todo: Abklären, ob getestet werden soll, ob im data-Attribut ein Geojson-Objekt abgespeichert ist:*/

    @Test (expected = ServiceException.class)
    public void missingContextInShowMessage() throws IOException, ServiceException {
        String showMessageWithoutContext = "{\"method\":\"show\"," +
                "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
        jsonConverter.stringToMessage(showMessageWithoutContext);
    }

    @Test (expected = ServiceException.class)
    public void missingDataInShowMessage() throws IOException, ServiceException {
        String showMessageWithoutData = "{\"method\":\"show\",\"context\":{\"afu_geschaeft\":\"3671951\"}}";
        jsonConverter.stringToMessage(showMessageWithoutData);
    }
    /*todo: Abklären, ob getestet werden soll, ob im data-Attribut ein Geojson-Objekt abgespeichert ist:*/

    @Test (expected = ServiceException.class)
    public void missingContextInCancelMessage() throws IOException, ServiceException {
        String cancelMessageWithoutContext = "{\"method\":\"cancel\"}";
        jsonConverter.stringToMessage(cancelMessageWithoutContext);
    }
    /*todo: Abklären, ob getestet werden soll, ob im context-Attribut ein Json-Objekt abgespeichert ist.*/

    @Test (expected = ServiceException.class)
    public void missingContextAttributeInChangedMessage() throws IOException, ServiceException {
        String messageWithMissingContext = "{\"method\":\"changed\"," +
                "\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
        jsonConverter.stringToMessage(messageWithMissingContext);
    }

    @Test (expected = ServiceException.class)
    public void missingPropertiesInDataWrittenMessage() throws IOException, ServiceException {
        String dataWrittenMessageWithoutProperties = "{\"method\":\"dataWritten\"}";
        jsonConverter.stringToMessage(dataWrittenMessageWithoutProperties);
    }
    /*todo: Abklären, ob getestet werden soll, ob im properties-Attribut ein Json-Objekt abgespeichert ist. */

    /*Todo: Abklären, ob bei der selectedMessage geprüft werden soll, ob die context_list wirklich ein Array ist */

    @Test (expected = ServiceException.class)
    public void missingCodeAttributeInErrorMessage() throws IOException, ServiceException {
        String errorMessageWithoutCode = "{\"method\":\"error\",\"message\":\"test Errormessage\"," +
                "\"userData\":{\"test\":\"3671951\"},\"nativeCode\":\"test nativeCode\"," +
                "\"technicalDetails\":\"test technicalDetails\"}";
        jsonConverter.stringToMessage(errorMessageWithoutCode);
    }

    @Test (expected = ServiceException.class)
    public void missingMessageAttributeInErrorMessage() throws IOException, ServiceException {
        String errorMessageWithoutMessage = "{\"method\":\"error\",\"code\":999}";
        jsonConverter.stringToMessage(errorMessageWithoutMessage);
    }
    /*Todo: Abklären, ob getestet werden soll, ob im userData-Attribut ein Json-Objekt abgespeicher ist.*/


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
        return createNode("{\"laufnr\":\"2017-820\",\"grundbuch\":\"Trimbach\"}");
    }
    private JsonNode createContextListNode () throws IOException {
        return createNode("[{\"bfs_num\":231,\"parz_num\":1951},{\"bfs_num\":231,\"parz_num\":2634}]");
    }

    private JsonNode createUserDataToNode() throws IOException {
        return createNode("{\"test\":\"3671951\"}");
    }



}