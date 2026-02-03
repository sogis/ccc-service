package ch.so.agi.cccservice.message;

import ch.so.agi.cccservice.session.SockConnection;

public class KeyChange {

    public static void sendKeyChangeToConnection(SockConnection receiver) {
        String msg = "{\n"
                + "    \"method\": \"keyChange\",\n"
                + "    \"newConnectionKey\": \"" + receiver.refreshKey() + "\"\n"
                + "}";
        receiver.sendMessage(msg);
    }
}
