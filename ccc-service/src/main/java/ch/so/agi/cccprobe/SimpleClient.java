package ch.so.agi.cccprobe;

import java.util.Scanner;

import ch.so.agi.cccservice.JsonConverter;
import ch.so.agi.cccservice.SessionId;
import ch.so.agi.cccservice.messages.AppConnectMessage;
import ch.so.agi.cccservice.messages.GisConnectMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

public class SimpleClient {

    public static final SessionId sessionId = new SessionId("{E11-TRALLALLA-UND-BLA-BLA-BLA-666}");

    public static void main(String[] args) throws Exception {
        StandardWebSocketClient appClient = new StandardWebSocketClient();
        AppClientHandler appSessionHandler = new AppClientHandler();

        appClient.doHandshake(appSessionHandler,"ws://localhost:8080/myHandler");

        AppConnectMessage appConnectMessage = new AppConnectMessage();
        appConnectMessage.setApiVersion("1.0");
        appConnectMessage.setSession(sessionId);
        appConnectMessage.setClientName("Axioma Mandant AfU");

        Thread.sleep(2000);
        appSessionHandler.sendMessage(appConnectMessage);

        StandardWebSocketClient gisClient = new StandardWebSocketClient();
        AppClientHandler gisSessionHandler = new AppClientHandler();

        gisClient.doHandshake(gisSessionHandler,"ws://localhost:8080/myHandler");

        GisConnectMessage gisConnectMessage = new GisConnectMessage();
        gisConnectMessage.setApiVersion("1.0");
        gisConnectMessage.setSession(sessionId);
        gisConnectMessage.setClientName("Gis Client");

        Thread.sleep(2000);
        gisSessionHandler.sendMessage(gisConnectMessage);

        Thread.sleep(2000);
        if (gisSessionHandler.getAppReady() != null && gisSessionHandler.getAppReady() == true && appSessionHandler.getAppReady() != null && appSessionHandler.getAppReady() == true) {
            System.exit(0);
        }
        System.exit(1);

    }
}