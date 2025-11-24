package ch.so.agi.cccservice.session;

import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MockWebSocketSession implements WebSocketSession {
    private final String key;
    private final Map<String, Object> attributes = new HashMap<>();
    private final List<String> sentTextMessages = new ArrayList<>();
    private boolean open = true;

    public MockWebSocketSession() {
        CryptoKey cryptoKey = new CryptoKey();
        this.key = cryptoKey.getKeyString();
    }

    public static MockWebSocketSession create(){
        return new MockWebSocketSession();
    }

    @Override
    public String getId() {
        return key;
    }

    @Override
    public URI getUri() {
        return null;
    }

    @Override
    public HttpHeaders getHandshakeHeaders() {
        return HttpHeaders.EMPTY;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public String getAcceptedProtocol() {
        return null;
    }

    @Override
    public void setTextMessageSizeLimit(int messageSizeLimit) {
        // no-op for tests
    }

    @Override
    public int getTextMessageSizeLimit() {
        return 0;
    }

    @Override
    public void setBinaryMessageSizeLimit(int messageSizeLimit) {
        // no-op for tests
    }

    @Override
    public int getBinaryMessageSizeLimit() {
        return 0;
    }

    @Override
    public List<WebSocketExtension> getExtensions() {
        return Collections.emptyList();
    }

    @Override
    public void sendMessage(WebSocketMessage<?> message) throws IOException {
        if (message instanceof TextMessage textMessage) {
            sentTextMessages.add(textMessage.getPayload());
        }
    }

    public List<String> getSentTextMessages() {
        return Collections.unmodifiableList(sentTextMessages);
    }

    public String getLastSentTextMessage() {
        if (sentTextMessages.isEmpty()) {
            return null;
        }
        return sentTextMessages.get(sentTextMessages.size() - 1);
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException {
        open = false;
    }

    @Override
    public void close(CloseStatus status) throws IOException {
        open = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MockWebSocketSession other)) {
            return false;
        }
        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "MockWebSocketSession{" +
                "key='" + key + '\'' +
                '}';
    }
}
