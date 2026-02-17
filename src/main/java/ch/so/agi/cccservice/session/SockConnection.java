package ch.so.agi.cccservice.session;

import java.io.IOException;

import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket connection between server
 * and either app or gis client.
 */
public final class SockConnection {
    public static final String PROTOCOL_V1 = "1.0";
    public static final String PROTOCOL_V12 = "1.2";

    public String getClientName() {
        return clientName;
    }

    /**
     * Name of the client, as declared
     * by the client in the handshake
     */
    private final String clientName;

    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Version of the CCC-Protocol used
     * by this client.
     */
    private final String apiVersion;

    /**
     * The key used to validate reconnect messages.
     */
    private final CryptoKey key;

    /**
     * The spring websocket connection
     */
    private WebSocketSession webSocketConnection;

    public SockConnection(String clientName, String protocolVersion, WebSocketSession webSocketConnection){
        this.clientName = clientName;
        assertValidApiVersion(protocolVersion);
        this.apiVersion = protocolVersion;
        this.key = new CryptoKey();
        this.webSocketConnection = webSocketConnection;
    }

    public synchronized WebSocketSession getWebSocketConnection() {
        return webSocketConnection;
    }

    private static void assertValidApiVersion(String apiVersion){
        boolean validProtocol = PROTOCOL_V1.equals(apiVersion) || PROTOCOL_V12.equals(apiVersion);

        if(!validProtocol)
            throw new RuntimeException(String.format("Protocol must be either %s or %s, but was %s", PROTOCOL_V1, PROTOCOL_V12, apiVersion));
    }

    public synchronized boolean isOpen(){
        if (webSocketConnection == null)
            return false;

        return  webSocketConnection.isOpen();
    }

    public String getConnectionKey(){
        return key.getKeyString();
    }

    /**
     * Synchronized sending of the message
     */
    public synchronized void sendMessage(String message){
        if (!isOpen()) {
            return;
        }
        TextMessage msg = new TextMessage(message);
        try {
            webSocketConnection.sendMessage(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Synchronized sending of a ping to the connection
     */
    public synchronized void sendPing(){
        PingMessage ping = new PingMessage();
        try {
            webSocketConnection.sendMessage(ping);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Refreshes the key and returns the new key.
     */
    public Object refreshKey() {
        key.refreshKey();
        return getConnectionKey();
    }

    public boolean keyEquals(String keyString) {
        return key.isEqual(keyString);
    }

    public synchronized void switchToNewWebSocketCon(WebSocketSession con){
        if(webSocketConnection == null)
            throw new IllegalStateException("Expected old connection to be present, but was null");

        WebSocketSession oldConnection = webSocketConnection;
        webSocketConnection = con;

        try {
            oldConnection.close();
        } catch (IOException e) {
            // Don't fail - the reconnect has already succeeded
        }
    }
}
