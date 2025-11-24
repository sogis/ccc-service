package ch.so.agi.cccservice.message.gis;

import ch.so.agi.cccservice.message.Connect;

public class ConnectGis extends Connect {
    public static final String MESSAGE_TYPE = "connectGis";
    ConnectGis(){
        super(MESSAGE_TYPE);
    }

    @Override
    protected String clientType() {
        return GIS_CLIENT_TYPENAME;
    }
}