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
        Session session = openSession(sessionUid, false, gisProtocolVersion);

        MockWebSocketSession appWebSocket = new MockWebSocketSession();
        SockConnection appConnection = new SockConnection("app-client", appProtocolVersion, appWebSocket);

        boolean added = session.tryToAddSecondConnection(appConnection, true);

        Sessions.addOrReplace(session);

        if(!added)
            throw new RuntimeException("Adding app-client failed");

        return session;
    }

    public static Session openSession(boolean openFromApp){
        return openSession(UUID.randomUUID(), openFromApp, SockConnection.PROTOCOL_V1);
    }

    public static Session openSession(UUID sessionUid, boolean openFromApp, String protocolVersion){
        String clientName = "app-client";
        if(!openFromApp)
            clientName = "gis-client";

        MockWebSocketSession socket = new MockWebSocketSession();
        SockConnection conn = new SockConnection(clientName, protocolVersion, socket);

        Session s = new Session(UUID.randomUUID(), conn, openFromApp);
        Sessions.addOrReplace(s);

        return s;
    }
}
