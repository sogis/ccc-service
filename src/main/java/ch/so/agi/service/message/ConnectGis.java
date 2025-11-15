package ch.so.agi.service.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectGis extends Message {

    public static final String MESSAGE = "connectGis";


    private UUID sessionUid;
    @JsonProperty("clientName")
    private String clientName;
    @JsonProperty("apiVersion")
    private String apiVersion;

    @JsonProperty("session")
    private void setSessionUidFromString(String session){
        this.sessionUid = uidFromString(session);
    }

    public UUID getSessionUid() {
        return sessionUid;
    }

    public String getClientName() {
        return clientName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public void process(WebSocketSession sourceConnection) {
        addClient(getSessionUid(), false, clientName, apiVersion, sourceConnection);
    }
}

