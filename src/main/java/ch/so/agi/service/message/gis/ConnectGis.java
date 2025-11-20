package ch.so.agi.service.message.gis;

import ch.so.agi.service.message.Connect;

public class ConnectGis extends Connect {
    public static final String MESSAGE_TYPE = "connectGis";
    ConnectGis(){
        super(MESSAGE_TYPE);
    }
}