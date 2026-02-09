package ch.so.agi.cccservice.session;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class SockConnectionTest {

    // --- Protokoll-Validierung ---

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

    // --- sendMessage ---

    @Test
    void sendMessage_sendsTextToWebSocket() {
        MockWebSocketSession socket = new MockWebSocketSession();
        SockConnection con = new SockConnection("test", SockConnection.PROTOCOL_V1, socket);

        con.sendMessage("hello");

        assertEquals("hello", socket.getLastSentTextMessage());
    }

    // --- isOpen ---

    @Test
    void isOpen_trueWhenWebSocketOpen() {
        SockConnection con = new SockConnection("test", SockConnection.PROTOCOL_V1, new MockWebSocketSession());

        assertTrue(con.isOpen());
    }

    @Test
    void isOpen_falseWhenWebSocketClosed() throws IOException {
        MockWebSocketSession socket = new MockWebSocketSession();
        SockConnection con = new SockConnection("test", SockConnection.PROTOCOL_V1, socket);

        socket.close();

        assertFalse(con.isOpen());
    }

    // --- Connection Key ---

    @Test
    void keyEquals_matchesOwnKey() {
        SockConnection con = new SockConnection("test", SockConnection.PROTOCOL_V1, new MockWebSocketSession());

        assertTrue(con.keyEquals(con.getConnectionKey()));
    }

    @Test
    void keyEquals_rejectsWrongKey() {
        SockConnection con = new SockConnection("test", SockConnection.PROTOCOL_V1, new MockWebSocketSession());

        assertFalse(con.keyEquals("wrongKey"));
    }

    @Test
    void refreshKey_changesKey() {
        SockConnection con = new SockConnection("test", SockConnection.PROTOCOL_V12, new MockWebSocketSession());
        String oldKey = con.getConnectionKey();

        con.refreshKey();

        assertNotEquals(oldKey, con.getConnectionKey());
        assertTrue(con.keyEquals(con.getConnectionKey()));
    }

    // --- switchToNewWebSocketCon ---

    @Test
    void switchToNewWebSocketCon_replacesConnection() {
        MockWebSocketSession oldSocket = new MockWebSocketSession();
        MockWebSocketSession newSocket = new MockWebSocketSession();
        SockConnection con = new SockConnection("test", SockConnection.PROTOCOL_V1, oldSocket);

        con.switchToNewWebSocketCon(newSocket);

        assertSame(newSocket, con.getWebSocketConnection());
    }

    @Test
    void switchToNewWebSocketCon_closesOldConnection() {
        MockWebSocketSession oldSocket = new MockWebSocketSession();
        MockWebSocketSession newSocket = new MockWebSocketSession();
        SockConnection con = new SockConnection("test", SockConnection.PROTOCOL_V1, oldSocket);

        con.switchToNewWebSocketCon(newSocket);

        assertFalse(oldSocket.isOpen(), "Old connection should be closed");
        assertTrue(newSocket.isOpen(), "New connection should stay open");
        assertSame(newSocket, con.getWebSocketConnection());
    }

    @Test
    void switchToNewWebSocketCon_succeedsEvenIfOldConnectionAlreadyClosed() throws IOException {
        // Alte Connection ist bereits geschlossen
        MockWebSocketSession oldSocket = new MockWebSocketSession();
        oldSocket.close();
        MockWebSocketSession newSocket = new MockWebSocketSession();
        SockConnection con = new SockConnection("test", SockConnection.PROTOCOL_V1, oldSocket);

        // Sollte trotzdem funktionieren
        assertDoesNotThrow(() -> con.switchToNewWebSocketCon(newSocket));
        assertSame(newSocket, con.getWebSocketConnection());
        assertTrue(newSocket.isOpen());
    }

    @Test
    void switchToNewWebSocketCon_throwsWhenOldConnectionIsNull() {
        SockConnection con = new SockConnection("test", SockConnection.PROTOCOL_V1, null);

        assertThrows(IllegalStateException.class,
                () -> con.switchToNewWebSocketCon(new MockWebSocketSession()));
    }
}
