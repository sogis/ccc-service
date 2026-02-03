package ch.so.agi.cccservice;

import ch.so.agi.cccservice.message.app.ConnectApp;
import ch.so.agi.cccservice.message.gis.ConnectGis;
import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;
import org.awaitility.Awaitility;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtil {

    /**
     * Helper to wait 1 up to 5000 milliseconds using Awaitility.
     */
    public static void wait(int millis){
        if(millis < 1 || millis > 5000)
            throw new IllegalArgumentException("Millis must be in the range (including) 1 to 5000");

        Awaitility.await()
                .timeout(millis + 500, TimeUnit.MILLISECONDS)
                .pollDelay(millis, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertTrue(true));
    }

    public static Session initSession(){
        return initSession(UUID.randomUUID());
    }

    public static Session initSession(UUID sessionUid){
        return initSession(sessionUid, SockConnection.PROTOCOL_V1, SockConnection.PROTOCOL_V1);
    }

    public static Session initSession(UUID sessionUid, String appProtocolVersion, String gisProtocolVersion){

        Session session = openSession(sessionUid, true, appProtocolVersion);
        connectClientToSession(sessionUid, false, gisProtocolVersion);

        return session;
    }

    private static Session connectClientToSession(UUID sessionUid, boolean fromAppClient, String protocolVersion){
        String connectTemplate = """
                {
                    "method": "$METHOD",
                    "clientName": "$CLIENT",
                    "apiVersion": "$VERSION",
                    "session": "{$SESSION}"
                }
                """;

        String method = ConnectApp.MESSAGE_TYPE;
        if(!fromAppClient)
            method = ConnectGis.MESSAGE_TYPE;

        String message = connectTemplate
                .replace("$METHOD", method)
                .replace("$CLIENT", method)
                .replace("$VERSION", protocolVersion)
                .replace("$SESSION", sessionUid.toString());

        MockWebSocketSession sender = new MockWebSocketSession();
        MessageHandler.handleMessage(sender, message);

        return Sessions.findByConnection(sender);
    }

    public static Session openSession(boolean openFromApp){
        return openSession(UUID.randomUUID(), openFromApp, SockConnection.PROTOCOL_V1);
    }

    public static Session openSession(UUID sessionUid, boolean openFromApp, String protocolVersion){
        return connectClientToSession(sessionUid, openFromApp, protocolVersion);
    }
}
