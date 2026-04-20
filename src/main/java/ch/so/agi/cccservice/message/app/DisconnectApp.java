package ch.so.agi.cccservice.message.app;

import ch.so.agi.cccservice.message.Disconnect;

public class DisconnectApp extends Disconnect {
    public static final String MESSAGE_TYPE = "disconnectApp";
    DisconnectApp() {
        super(MESSAGE_TYPE);
    }

    @Override
    protected String clientType() {
        return APP_CLIENT_TYPENAME;
    }

    @Override
    protected boolean isAppClient() {
        return true;
    }
}
