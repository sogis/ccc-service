package ch.so.agi.cccservice.session;

import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * WebSocket connection between server
 * and either app or gis client.
 */
public class SockConnection {
    public static final String PROTOCOL_V1 = "1.0";
    public static final String PROTOCOL_V2 = "2.0";

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

    public WebSocketSession getWebSocketConnection() {
        return webSocketConnection;
    }

    private static void assertValidApiVersion(String apiVersion){
        boolean validProtocol = PROTOCOL_V1.equals(apiVersion) || PROTOCOL_V2.equals(apiVersion);

        if(!validProtocol)
            throw new RuntimeException(String.format("Protocol must be either %s or %s, but was %s", PROTOCOL_V1, PROTOCOL_V2, apiVersion));
    }

    public boolean isOpen(){
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

    public void switchToNewWebSocketCon(WebSocketSession con){
        if(webSocketConnection == null)
            throw new IllegalStateException("Expected old connection to be present, but was null");

        /*
        try{
            webSocketConnection.close();
        }
        catch(IOException e){
            throw new RuntimeException("Exception when closing connection", e);
        }

         */

        webSocketConnection = con;
    }
}
