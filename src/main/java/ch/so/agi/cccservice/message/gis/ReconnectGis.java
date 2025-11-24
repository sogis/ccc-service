package ch.so.agi.cccservice.message.gis;

import ch.so.agi.cccservice.message.Reconnect;

public class ReconnectGis extends Reconnect {
    public static final String MESSAGE_TYPE = "reconnectGis";
    public ReconnectGis(){
        super(MESSAGE_TYPE);
    }

    @Override
    protected String clientType() {
        return GIS_CLIENT_TYPENAME;
    }
}
