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

    @Test
    public void appConnectMessageToString() throws IOException {
        AppConnectMessage appConnectMessage = new AppConnectMessage();
        appConnectMessage.setApiVersion("1.0");
        appConnectMessage.setSession(new SessionId("{E9-TRALLALLA-UND-BLA-BLA-BLA-666}"));
        appConnectMessage.setClientName("Axioma Mandant AfU");

        JsonConverter jsonConverter = new JsonConverter();
        String resultingJson = jsonConverter.messageToString(appConnectMessage);

        assertTrue(resultingJson.equals("{\"method\":\"appConnect\"," +
                                        "\"session\":{\"sessionId\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"}," +
                                        "\"clientName\":\"Axioma Mandant AfU\"," +
                                        "\"apiVersion\":\"1.0\"}"));

    }

    @Test
    public void stringToAppConnectMessage() throws IOException, MethodeException {
        String json = "{\"method\":\"appConnect\"," +
                      "\"session\":{\"sessionId\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"}," +
                      "\"clientName\":\"Axioma Mandant AfU\"," +
                      "\"apiVersion\":\"1.0\"}";
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(json) instanceof AppConnectMessage);
    }

    @Test
    public void gisConnectMessageToString() throws IOException {
        GisConnectMessage gisConnectMessage = new GisConnectMessage();
        gisConnectMessage.setApiVersion("1.0");
        gisConnectMessage.setSession(new SessionId("{E9-TRALLALLA-UND-BLA-BLA-BLA-666}"));
        gisConnectMessage.setClientName("Web GIS Client");

        JsonConverter jsonConverter = new JsonConverter();
        String resultingJson = jsonConverter.messageToString(gisConnectMessage);

        assertTrue(resultingJson.equals("{\"method\":\"gisConnect\"," +
                "\"session\":{\"sessionId\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"}," +
                "\"clientName\":\"Web GIS Client\"," +
                "\"apiVersion\":\"1.0\"}"));
    }

    @Test
    public void stringToGisConnectMessage() throws IOException, MethodeException {
        String json = "{\"method\":\"gisConnect\"," +
                "\"session\":{\"sessionId\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"}," +
                "\"clientName\":\"Web GIS Client\"," +
                "\"apiVersion\":\"1.0\"}";
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(json) instanceof GisConnectMessage);
    }

    @Test
    public void cancelMessageToString() throws IOException {
        CancelMessage cancelMessage = new CancelMessage();
        cancelMessage.setContext(createContextNode());
        JsonConverter jsonConverter = new JsonConverter();
        String resultingJson = jsonConverter.messageToString(cancelMessage);
        assertTrue(resultingJson.equals("{\"method\":\"cancel\",\"context\":{\"afu_geschaeft\":\"3671951\"}}"));
    }

    @Test
    public void stringToCancelMessage() throws IOException, MethodeException {
        String json = "{\"method\":\"cancel\",\"context\":{\"afu_geschaeft\":\"3671951\"}}";
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(json) instanceof CancelMessage);
    }

    @Test
    public void changedMessageToString() throws IOException {
        ChangedMessage changedMessage = new ChangedMessage();
        changedMessage.setData(createDataNode());
        changedMessage.setContext(createContextNode());
        JsonConverter jsonConverter = new JsonConverter();
        String resultingJson = jsonConverter.messageToString(changedMessage);
        assertTrue(resultingJson.equals("{\"method\":\"changed\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}"));
    }

    @Test
    public void stringToChangedMessage() throws IOException, MethodeException {
        String json = "{\"method\":\"changed\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(json) instanceof ChangedMessage);
    }

    @Test
    public void createMessageToString() throws IOException {
        CreateMessage createMessage = new CreateMessage();
        createMessage.setZoomTo(createZoomToNode());
        createMessage.setContext(createContextNode());
        JsonConverter jsonConverter = new JsonConverter();
        String resultingJson = jsonConverter.messageToString(createMessage);
        assertTrue(resultingJson.equals("{\"method\":\"create\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"zoomTo\":{\"gemeinde\":2542}}"));
    }

    @Test
    public void stringToCreateMessage() throws IOException, MethodeException {
        String json = "{\"method\":\"create\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"zoomTo\":{\"gemeinde\":2542}}";
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(json) instanceof CreateMessage);
    }

    @Test
    public void dataWrittenToString() throws IOException {
        DataWrittenMessage dataWrittenMessage = new DataWrittenMessage();
        dataWrittenMessage.setProperties(createPropertiesNode());
        JsonConverter jsonConverter = new JsonConverter();
        String resultingJson = jsonConverter.messageToString(dataWrittenMessage);
        assertTrue(resultingJson.equals("{\"method\":\"dataWritten\",\"properties\":{\"laufnr\":\"2017-820\",\"grundbuch\":\"Trimbach\"}}"));
    }

    @Test
    public void stringToDataWrittenMessage() throws IOException, MethodeException {
        String json = "{\"method\":\"dataWritten\",\"properties\":{\"laufnr\":\"2017-820\",\"grundbuch\":\"Trimbach\"}}";
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(json) instanceof DataWrittenMessage);
    }

    @Test
    public void editMessageToString() throws IOException {
        EditMessage editMessage = new EditMessage();
        editMessage.setData(createDataNode());
        editMessage.setContext(createContextNode());
        JsonConverter jsonConverter = new JsonConverter();
        String resultingJson = jsonConverter.messageToString(editMessage);
        assertTrue(resultingJson.equals("{\"method\":\"edit\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}"));
    }

    @Test
    public void stringToEditMessage() throws IOException, MethodeException {
        String json = "{\"method\":\"edit\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(json) instanceof EditMessage);
    }

    @Test
    public void errorMessageToString() throws IOException {

    }

    @Test
    public void stringToErrorMessage () throws IOException, MethodeException {

    }

    @Test
    public void readyMessageToString() throws IOException {
        ReadyMessage readyMessage = new ReadyMessage();
        readyMessage.setApiVersion("1.0");
        JsonConverter jsonConverter = new JsonConverter();
        String resultingJson = jsonConverter.messageToString(readyMessage);
        assertTrue(resultingJson.equals("{\"method\":\"ready\",\"apiVersion\":\"1.0\"}"));
    }

    @Test
    public void stringToReadyMessage() throws IOException, MethodeException {
        String json = "{\"method\":\"ready\",\"apiVersion\":\"1.0\"}";
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(json) instanceof ReadyMessage);
    }

    @Test
    public void selectedMessageToString() throws IOException {
        SelectedMessage selectedMessage = new SelectedMessage();
        selectedMessage.setContext_list(createContextListNode());
        JsonConverter jsonConverter = new JsonConverter();
        String resultingJson = jsonConverter.messageToString(selectedMessage);
        assertTrue(resultingJson.equals("{\"method\":\"selected\",\"context_list\":[{\"bfs_num\":231,\"parz_num\":1951},{\"bfs_num\":231,\"parz_num\":2634}]}"));
    }

    @Test
    public void stringToSelectedMessage() throws IOException, MethodeException {
        String json = "{\"method\":\"selected\",\"context_list\":[{\"bfs_num\":231,\"parz_num\":1951},{\"bfs_num\":231,\"parz_num\":2634}]}";
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(json) instanceof SelectedMessage);
    }

    @Test
    public void showMessageToString() throws IOException {
        ShowMessage showMessage = new ShowMessage();
        showMessage.setData(createDataNode());
        showMessage.setContext(createContextNode());
        JsonConverter jsonConverter = new JsonConverter();
        String resultingJson = jsonConverter.messageToString(showMessage);
        assertTrue(resultingJson.equals("{\"method\":\"show\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}"));
    }

    @Test
    public void stringToShowMessage() throws IOException, MethodeException {
        String json = "{\"method\":\"show\",\"context\":{\"afu_geschaeft\":\"3671951\"},\"data\":{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}}";
        JsonConverter jsonConverter = new JsonConverter();
        assertTrue(jsonConverter.stringToMessage(json) instanceof ShowMessage);
    }

    @Test (expected = JsonEOFException.class)
    public void corruptJsonEofTest() throws IOException, MethodeException {
        String json = "{\"method\":\"appConnect\"," +
                "\"session\":{\"sessionId\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"}," +
                "\"clientName\":\"Axioma Mandant AfU\"," +
                "\"apiVersion\":\"1.0\""; //Es fehlt die letzte Klammer }
        JsonConverter jsonConverter = new JsonConverter();
        jsonConverter.stringToMessage(json);
    }

    @Test (expected = MethodeException.class)
    public void noMethodTest() throws IOException, MethodeException {
        String json = "{" + //Keine Methode
                "\"session\":{\"sessionId\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"}," +
                "\"clientName\":\"Axioma Mandant AfU\"," +
                "\"apiVersion\":\"1.0\"}";
        JsonConverter jsonConverter = new JsonConverter();
        jsonConverter.stringToMessage(json);
    }

    @Test (expected = JsonParseException.class)
    public void corruptJsonTest() throws IOException, MethodeException {
        String json = "{\"method\":\"appConnect\"," +
                "\"session\":{\"sessionId\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"}," +
                "\"clientNameAxioma Mandant AfU\"," + //Hier ist doch was faul...
                "\"apiVersion\":\"1.0\"}";
        JsonConverter jsonConverter = new JsonConverter();
        jsonConverter.stringToMessage(json);
    }


    //TEST-HILFSKLASSEN

    public JsonNode createNode (String str) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readTree(str);
        return obj;
    }
    public JsonNode createDataNode () throws IOException {
        JsonNode dataNode = createNode("{\"type\":\"Point\",\"coordinates\":\"[2609190,1226652]\"}");
        return dataNode;
    }
    public JsonNode createContextNode () throws IOException {
        JsonNode contextNode = createNode("{\"afu_geschaeft\":\"3671951\"}");
        return contextNode;
    }
    public JsonNode createZoomToNode () throws IOException {
        JsonNode zoomToNode = createNode("{\"gemeinde\":2542}");
        return zoomToNode;
    }
    public JsonNode createPropertiesNode () throws IOException {
        JsonNode propertiesNode = createNode("{\"laufnr\":\"2017-820\",\"grundbuch\":\"Trimbach\"}");
        return propertiesNode;
    }
    public JsonNode createContextListNode () throws IOException {
        JsonNode contextListNode = createNode("[{\"bfs_num\":231,\"parz_num\":1951},{\"bfs_num\":231,\"parz_num\":2634}]");
        return contextListNode;
    }



}
