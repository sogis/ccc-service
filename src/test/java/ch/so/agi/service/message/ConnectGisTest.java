package ch.so.agi.service.message;

import ch.so.agi.service.message.gis.ConnectGis;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ConnectGisTest {
    @Test
    void validJson_parses(){
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