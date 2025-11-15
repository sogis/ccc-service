package ch.so.agi.service.session;

import ch.so.agi.service.message.exception.MessageParseException;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket connection between server
 * and either app or gis client.
 */
public class SockConnection {
    private static final String PROTOCOL_V1 = "1.0";
    private static final String PROTOCOL_V2 = "2.0";
    /**
     * Name of the client, as declared
     * by the client in the handshake
     */
    private final String clientName;

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
}
