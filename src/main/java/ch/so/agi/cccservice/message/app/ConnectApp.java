package ch.so.agi.cccservice.message.app;

import ch.so.agi.cccservice.message.Connect;

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