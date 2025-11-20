package ch.so.agi.service.message;

import ch.so.agi.service.message.app.ConnectApp;
import ch.so.agi.service.message.gis.ConnectGis;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConnectTest {
    @Test
    void app_validJson_parses(){
        String validJson = """
                {
                    "method": "connectApp",
                    "clientName": "Axioma Mandant AfU",
                    "apiVersion": "1.0",
                    "session": "{019a835f-87b7-7969-ab37-53a4333c8558}"
                }
                """;

        ConnectApp con = (ConnectApp) Message.forJsonString(validJson);

        assertEquals("Axioma Mandant AfU", con.getClientName());
        assertEquals("1.0", con.getApiVersion());
        assertEquals(UUID.fromString("019a835f-87b7-7969-ab37-53a4333c8558"), con.getSessionUid());
    }

    @Test
    void gis_validJson_parses(){
        String validJson = """
                {
                    "method": "connectGis",
                    "clientName": "WGC",
                    "apiVersion": "2.0",
                    "session": "{019a835f-87b7-7969-ab37-53a4333c8558}"
                }
                """;

        ConnectGis con = (ConnectGis) Message.forJsonString(validJson);

        assertEquals("WGC", con.getClientName());
        assertEquals("2.0", con.getApiVersion());
        assertEquals(UUID.fromString("019a835f-87b7-7969-ab37-53a4333c8558"), con.getSessionUid());
    }

}