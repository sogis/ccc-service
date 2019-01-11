package ch.so.agi.cccprobe;

import ch.so.agi.cccservice.SessionId;
import ch.so.agi.cccservice.messages.ConnectAppMessage;
import ch.so.agi.cccservice.messages.ConnectGisMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

/**
 * Main class of the tool for testing the liveness of the ccc-service.
 * The liveness is checked, by executing a basic happy flow of the ccc protocol.
 */
public class ProbeTool {

    public static final String GIS_CLIENT_NAME = "ProbeTool GIS";
    public static final String CCC_PROTOCOL_VERSION = "1.0";
    public static final String APP_CLIENT_NAME = "ProbeTool APP";
    private static final String DEFAULT_ENDPOINT = "ws://localhost:8080/ccc-service";
    public static final SessionId sessionId = new SessionId("{314e0cc3-1d26-47d1-8cd4-3e7dd88e643d}");
    Logger logger = LoggerFactory.getLogger(ProbeTool.class);

    public static void main(String[] args) throws Exception {
        new ProbeTool().mymain(args);
    }
    public void mymain(String[] args) throws Exception {
        StandardWebSocketClient appClient = new StandardWebSocketClient();
        AppClientHandler appSessionHandler = new AppClientHandler(APP_CLIENT_NAME);
        String endpoint=DEFAULT_ENDPOINT;
        if(args.length==1) {
            endpoint=args[0];
        }
        appClient.doHandshake(appSessionHandler,endpoint);

        ConnectAppMessage appConnectMessage = new ConnectAppMessage();
        appConnectMessage.setApiVersion(CCC_PROTOCOL_VERSION);
        appConnectMessage.setSession(sessionId);
        appConnectMessage.setClientName(APP_CLIENT_NAME);

        Thread.sleep(2000);
        if (appSessionHandler.isConnected()) {
            appSessionHandler.sendMessage(appConnectMessage);
        }
        else {
            logger.error("Session not open. Could not send "+appConnectMessage.getMethod());
        }


        StandardWebSocketClient gisClient = new StandardWebSocketClient();
        AppClientHandler gisSessionHandler = new AppClientHandler(GIS_CLIENT_NAME);

        gisClient.doHandshake(gisSessionHandler,endpoint);

        ConnectGisMessage gisConnectMessage = new ConnectGisMessage();
        gisConnectMessage.setApiVersion(CCC_PROTOCOL_VERSION);
        gisConnectMessage.setSession(sessionId);
        gisConnectMessage.setClientName(GIS_CLIENT_NAME);

        Thread.sleep(2000);
        if (gisSessionHandler.isConnected()) {
            gisSessionHandler.sendMessage(gisConnectMessage);
        }
        else {
            logger.error("Session not open. Could not send "+gisConnectMessage.getMethod());
        }

        Thread.sleep(2000);
        if (gisSessionHandler.getAppReady() != null && gisSessionHandler.getAppReady() == true && appSessionHandler.getAppReady() != null && appSessionHandler.getAppReady() == true) {
            System.exit(0);
        }

        System.exit(1);

    }
}