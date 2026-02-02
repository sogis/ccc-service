package ch.so.agi.cccservice.session;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SockConnectionTest {

    @Test
    void protocolV1_OK() {
        SockConnection con = new SockConnection("testClient", SockConnection.PROTOCOL_V1, new MockWebSocketSession());

        assertEquals(SockConnection.PROTOCOL_V1, con.getApiVersion());
    }

    @Test
    void protocolV12_OK() {
        SockConnection con = new SockConnection("testClient", SockConnection.PROTOCOL_V12, new MockWebSocketSession());

        assertEquals(SockConnection.PROTOCOL_V12, con.getApiVersion());
    }

    @Test
    void invalidProtocol_throws() {
        assertThrows(RuntimeException.class,
                () -> new SockConnection("testClient", "2.0", new MockWebSocketSession()));
    }

    @Test
    void nullProtocol_throws() {
        assertThrows(RuntimeException.class,
                () -> new SockConnection("testClient", null, new MockWebSocketSession()));
    }
}
