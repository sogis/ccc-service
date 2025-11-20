package ch.so.agi.service.message.gis;

import ch.so.agi.service.message.Reconnect;

public class ReconnectGis extends Reconnect {
    public static final String MESSAGE_TYPE = "reconnectGis";
    public ReconnectGis(){
        super(MESSAGE_TYPE);
    }
}
