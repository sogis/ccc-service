package ch.so.agi.cccservice.message.app;

import ch.so.agi.cccservice.message.Reconnect;

public class ReconnectApp extends Reconnect {
    public static final String MESSAGE_TYPE = "reconnectApp";
    public ReconnectApp(){
        super(MESSAGE_TYPE);
    }

    @Override
    protected String clientType() {
        return APP_CLIENT_TYPENAME;
    }
}

