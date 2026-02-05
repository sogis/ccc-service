package ch.so.agi.cccservice.message;

import ch.so.agi.cccservice.session.SockConnection;

public class KeyChange {
    private KeyChange() {
        /* This utility class should not be instantiated */
    }

    public static final String METHOD_TYPE = "keyChange";

    public static void sendKeyChangeToConnection(SockConnection receiver) {
        String msg = "{\n"
                + "    \"method\": \"" + METHOD_TYPE + "\",\n"
                + "    \"newConnectionKey\": \"" + receiver.refreshKey() + "\"\n"
                + "}";
        receiver.sendMessage(msg);
    }
}
