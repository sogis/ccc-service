package ch.so.agi.cccservice.health;

import ch.so.agi.cccservice.message.SessionReady;
import ch.so.agi.cccservice.message.app.ChangeLayerVisibility;
import ch.so.agi.cccservice.message.gis.GeoObjectSelected;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * WebSocket client used by health checks to interact with the CCC service.
 * Provides basic helpers to connect, reconnect and send error notifications
 * as either an APP or GIS client.
 */
public class SocketClient extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(SocketClient.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public enum ClientType {
        APP("connectApp", "reconnectApp"),
        GIS("connectGis", "reconnectGis");

        private final String connectMethod;
        private final String reconnectMethod;

        ClientType(String connectMethod, String reconnectMethod) {
            this.connectMethod = connectMethod;
            this.reconnectMethod = reconnectMethod;
        }
    }

    private final StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
    private final String baseAddress;

    private WebSocketSession sockSession;

    private volatile String latestConnectionKey;
    private volatile Integer sessionNr;

    private ClientType clientType;

    public SocketClient(String baseAddress, ClientType clientType) {
        this.baseAddress = baseAddress;
        this.clientType = clientType;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("Transport error on websocket client", exception);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        /*
        this.sockSession = null;


        connectOrReconnectWebSocket();
        reconnectCCC();

         */
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode payload = OBJECT_MAPPER.readTree(message.getPayload());
        String method = payload.path("method").asText(null);

        if (method == null) {
            log.warn("Received websocket message without method: {}", message.getPayload());
            return;
        }

        switch (method) {
            case "keyChange" -> handleKeyChange(payload);
            case SessionReady.METHOD_TYPE -> handleSessionReady(payload);
            default -> log.warn("Ignoring unhandled websocket message '{}'. Message details: {}", method, message);
        }
    }

    public synchronized void connectCCC(UUID sessionUid, String clientName, String apiVersion, ClientType clientType) {
        if(!webSocketIsOpen())
            connectWebSocket();

        ObjectNode connectPayload = OBJECT_MAPPER.createObjectNode();
        connectPayload.put("method", clientType.connectMethod);
        connectPayload.put("session", formatSessionUid(sessionUid));
        connectPayload.put("clientName", clientName);
        connectPayload.put("apiVersion", apiVersion);

        sendPayload(sockSession, connectPayload);
    }

    public synchronized void reconnectCCC() {
        awaitSessionReady();

        if(!webSocketIsOpen())
            connectWebSocket();

        ObjectNode reconnectPayload = OBJECT_MAPPER.createObjectNode();
        reconnectPayload.put("method", clientType.reconnectMethod);
        reconnectPayload.put("oldConnectionKey", latestConnectionKey);
        reconnectPayload.put("oldSessionNumber", sessionNr);

        sendPayload(sockSession, reconnectPayload);
    }

    public synchronized void sendMinimalCCCMessage() {
        awaitSessionReady();
        assertSocketOpen();

        ObjectNode json = OBJECT_MAPPER.createObjectNode();

        if(clientType == ClientType.GIS){
            json.put("method", GeoObjectSelected.MESSAGE_TYPE);
        }
        else{
            json.put("method", ChangeLayerVisibility.MESSAGE_TYPE);
            json.put("layerIdentifier", "fuu");
            json.put("visible", false);
        }

        sendPayload(sockSession, json);
    }

    private void connectWebSocket(){
        CompletableFuture<WebSocketSession> future = webSocketClient.execute(this, baseAddress);
        //Await open connection
        this.sockSession = future.join();
    }

    private void assertSocketOpen(){
        if(!webSocketIsOpen())
            throw new RuntimeException("Websocket connection is expected to be open but is closed");
    }

    public boolean webSocketIsOpen(){
        boolean isNullOrClosed = (this.sockSession == null || !this.sockSession.isOpen());
        return !isNullOrClosed;
    }

    public void closeWebSocket(){
        if(sockSession == null || !sockSession.isOpen())
            return;

        try {
            sockSession.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendPayload(WebSocketSession webSocketSession, ObjectNode payload) {
        try {
            webSocketSession.sendMessage(new TextMessage(payload.toString()));
        } catch (IOException e) {
            throw new IllegalStateException("Could not send payload to cccservice", e);
        }
    }

    private void handleKeyChange(JsonNode payload) {
        latestConnectionKey = payload.path("newConnectionKey").asText(null);
        log.debug("Received key change with new connection key: {}", latestConnectionKey);
    }

    private void handleSessionReady(JsonNode payload) {

        this.latestConnectionKey = payload.path("connectionKey").asText(null);
        this.sessionNr = Integer.parseInt(
                payload.path("sessionNr").asText(null)
        );

        log.debug("Received session ready notification");
    }

    private String formatSessionUid(UUID sessionUid) {
        return "{" + sessionUid + "}";
    }

    private void awaitSessionReady() {
        Awaitility.await()
                .timeout(2000, TimeUnit.MILLISECONDS)
                .pollDelay(50, TimeUnit.MILLISECONDS)
                .until(() -> sessionNr != null);
    }

    public Integer getSessionNr() {
        return sessionNr;
    }
}