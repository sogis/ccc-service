package ch.so.agi.service.message;

import ch.so.agi.service.session.SockConnection;

public class KeyChange {

    private static final String KEY_CHANGE = """
            {
                "method": "keyChange",
                "newConnectionKey": "%s",
            }
            """;

    public static void sendKeyChangeToConnection(SockConnection receiver) {
        String msg = String.format(KEY_CHANGE, receiver.refreshKey());
        receiver.sendMessage(msg);
    }
}
