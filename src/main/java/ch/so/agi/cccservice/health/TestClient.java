package ch.so.agi.cccservice.health;

import ch.so.agi.cccservice.WebSocketConfig;
import ch.so.agi.cccservice.session.SockConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.UUID;

public class TestClient {

    private static final Logger log = LoggerFactory.getLogger(LivenessProbe.class);

    private SocketClient gisClient;
    private SocketClient appClient;

    public TestClient(){
        connectClients();
    }

    private void connectClients(){
        String adr = "ws://localhost:" + WebServerPort.getPort() + WebSocketConfig.CCC_SOCKET_PATH;

        this.gisClient = new SocketClient(adr, SocketClient.ClientType.GIS);
        this.appClient = new SocketClient(adr, SocketClient.ClientType.APP);

        UUID sesUid = UUID.randomUUID();

        gisClient.connectCCC(sesUid, "probe-gis", SockConnection.PROTOCOL_V12, SocketClient.ClientType.GIS);
        appClient.connectCCC(sesUid, "probe-app", SockConnection.PROTOCOL_V12, SocketClient.ClientType.APP);

        log.info("Connected to app and gis. Sessions: {},{}.", appClient.getSessionNr(), gisClient.getSessionNr());
    }

    public void reconnectAndSend() {
        assertConnected();

        gisClient.reconnectCCC();
        gisClient.sendMinimalCCCMessage();

        appClient.reconnectCCC();
        appClient.sendMinimalCCCMessage();

        log.info("Sent message from both app and gis client after reconnect");
    }

    private void assertConnected() {
        boolean gisClosed = !gisClient.webSocketIsOpen();
        boolean appClosed = !appClient.webSocketIsOpen();

        if(gisClosed || appClosed)
            connectClients();
    }

    public Integer getSessionNr(){
        return appClient.getSessionNr();
    }

    public void closeConnection(boolean appConnection) {
        if(appConnection)
            appClient.closeWebSocket();
        else
            gisClient.closeWebSocket();
    }
}


