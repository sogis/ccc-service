package ch.so.agi.service.message.app;

import ch.so.agi.service.message.Reconnect;

public class ReconnectApp extends Reconnect {
    public static final String MESSAGE_TYPE = "reconnectApp";
    public ReconnectApp(){
        super(MESSAGE_TYPE);
    }
}

