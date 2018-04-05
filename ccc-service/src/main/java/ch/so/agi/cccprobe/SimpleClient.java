package ch.so.agi.cccprobe;

import ch.so.agi.cccservice.SessionId;
import ch.so.agi.cccservice.messages.ConnectAppMessage;
import ch.so.agi.cccservice.messages.ConnectGisMessage;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

/**
 * A simple websocket-client for testing the connection with the ccc-service from AGI Solothurn.
 */
public class SimpleClient {

    public static final SessionId sessionId = new SessionId("{E11-TRALLALLA-UND-BLA-BLA-BLA-666}");

    /**
     * The main and only class
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        StandardWebSocketClient appClient = new StandardWebSocketClient();
        AppClientHandler appSessionHandler = new AppClientHandler();

        appClient.doHandshake(appSessionHandler,"ws://localhost:8080/myHandler");

        ConnectAppMessage appConnectMessage = new ConnectAppMessage();
        appConnectMessage.setApiVersion("1.0");
        appConnectMessage.setSession(sessionId);
        appConnectMessage.setClientName("Axioma Mandant AfU");

        Thread.sleep(2000);
        if (appSessionHandler.isConnected(appSessionHandler)) {
            appSessionHandler.sendMessage(appConnectMessage);
        }
        else {
            System.out.println("Session not open. Could not send appConnectMessage");
        }


        StandardWebSocketClient gisClient = new StandardWebSocketClient();
        AppClientHandler gisSessionHandler = new AppClientHandler();

        gisClient.doHandshake(gisSessionHandler,"ws://localhost:8080/myHandler");

        ConnectGisMessage gisConnectMessage = new ConnectGisMessage();
        gisConnectMessage.setApiVersion("1.0");
        gisConnectMessage.setSession(sessionId);
        gisConnectMessage.setClientName("Gis Client");

        Thread.sleep(2000);
        if (gisSessionHandler.isConnected(gisSessionHandler)) {
            gisSessionHandler.sendMessage(gisConnectMessage);
        }
        else {
            System.out.println("Session not open. Could not send gisConnectMessage");
        }

        Thread.sleep(2000);
        if (gisSessionHandler.getAppReady() != null && gisSessionHandler.getAppReady() == true && appSessionHandler.getAppReady() != null && appSessionHandler.getAppReady() == true) {
            System.exit(0);
        }

        System.exit(1);

    }
}