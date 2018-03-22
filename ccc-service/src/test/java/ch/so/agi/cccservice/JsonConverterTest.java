package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.AppConnectMessage;
import ch.so.agi.cccservice.messages.GisConnectMessage;
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


}
