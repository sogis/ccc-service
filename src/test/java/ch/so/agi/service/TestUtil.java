package ch.so.agi.service;

import ch.so.agi.service.session.MockWebSocketSession;
import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import ch.so.agi.service.session.SockConnection;
import org.awaitility.Awaitility;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtil {

    /**
     * Helper to wait 1 up to 5000 milliseconds using Awaitility.
     */
    public static void wait(int millis){
        if(millis < 1 || millis > 5000)
            throw new IllegalArgumentException("Millis must be in the range (including) 1 to 5000");

        Awaitility.await()
                .timeout(millis, TimeUnit.MILLISECONDS)
                .pollDelay(millis - 1, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertTrue(true));
    }

    public static Session initSession(){
        return initSession(UUID.randomUUID());
    }

    public static Session initSession(UUID sessionUid){
        return initSession(sessionUid, SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);
    }

    public static Session initSession(UUID sessionUid, String appProtocolVersion, String gisProtocolVersion){
        MockWebSocketSession gisWebSocket = new MockWebSocketSession();
        SockConnection gisConnection = new SockConnection("gis-client", gisProtocolVersion, gisWebSocket);
        Session session = new Session(sessionUid, gisConnection, false);

        MockWebSocketSession appWebSocket = new MockWebSocketSession();
        SockConnection appConnection = new SockConnection("app-client", appProtocolVersion, appWebSocket);

        boolean added = session.tryToAddSecondConnection(appConnection, true);

        Sessions.addOrReplace(session);

        if(!added)
            throw new RuntimeException("Adding app-client failed");

        return session;
    }

    public static void jsonStringEquals(String firstJson, String secondJson){

    }

    public class JsonComparator {

        public static void jsonStringEquals(String firstJson, String secondJson) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode tree1 = mapper.readTree(firstJson);
                JsonNode tree2 = mapper.readTree(secondJson);

                if (tree1.equals(tree2)) {
                    System.out.println("JSON strings are equal.");
                } else {
                    System.out.println("JSON strings are NOT equal.");
                }
            } catch (Exception e) {
                System.out.println("Invalid JSON input or parsing error: " + e.getMessage());
            }
        }

        // Example usage
        public static void main(String[] args) {
            String json1 = "{\"name\":\"Alice\",\"age\":30}";
            String json2 = "{  \"age\" : 30 , \"name\" : \"Alice\" }";

            jsonStringEquals(json1, json2); // Should print "JSON strings are equal."
        }
    }

}
