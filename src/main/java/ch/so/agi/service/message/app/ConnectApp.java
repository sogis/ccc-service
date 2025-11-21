package ch.so.agi.service.message.app;

import ch.so.agi.service.message.Connect;

public class ConnectApp extends Connect {
    public static final String MESSAGE_TYPE = "connectApp";
    ConnectApp(){
        super(MESSAGE_TYPE);
    }

    @Override
    protected String clientType() {
        return APP_CLIENT_TYPENAME;
    }
}