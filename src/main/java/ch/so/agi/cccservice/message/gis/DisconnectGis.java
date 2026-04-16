package ch.so.agi.cccservice.message.gis;

import ch.so.agi.cccservice.message.Disconnect;

public class DisconnectGis extends Disconnect {
    public static final String MESSAGE_TYPE = "disconnectGis";
    DisconnectGis() {
        super(MESSAGE_TYPE);
    }

    @Override
    protected String clientType() {
        return GIS_CLIENT_TYPENAME;
    }
}
