package ch.so.agi.service.session;

import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket connection between server
 * and either app or gis client.
 */
public class Connection {
    private static final String PROTOCOL_V1 = "1.0";
    private static final String PROTOCOL_V2 = "2.0";
    /**
     * Name of the client, as declared
     * by the client in the handshake
     */
    private String clientName;

    /**
     * Version of the CCC-Protocol used
     * by this client.
     */
    private String protocolVersion;

    /**
     * The key used to validate reconnect messages.
     */
    private CryptoKey key;

    /**
     * The spring websocket connection
     */
    private WebSocketSession socketConnection;

    public WebSocketSession getSocketConnection() {
        return socketConnection;
    }

    public void setSocketConnection(WebSocketSession socketConnection) {
        this.socketConnection = socketConnection;
    }

}
