package ch.so.agi.service.message;

import ch.so.agi.service.session.Session;
import ch.so.agi.service.session.Sessions;
import ch.so.agi.service.session.SockConnection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.WebSocketSession;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotifyError extends Message {

    public static final String MESSAGE_TYPE = "notifyError";

    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String errMessage;
    @JsonProperty("userData")
    private JsonNode userData;
    @JsonProperty("nativeCode")
    private String nativeCode;
    @JsonProperty("technicalDetails")
    private String technicalDetails;

    // Required no-args constructor
    public NotifyError() {}

    public String getTechnicalDetails() {
        return technicalDetails;
    }

    public String getNativeCode() {
        return nativeCode;
    }

    public JsonNode getUserData() {
        return userData;
    }

    @Override
    public String getRawMessage() {
        return errMessage;
    }

    public int getCode() {
        return code;
    }

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = Sessions.findByConnection(sourceConnection);
        s.assertConnected();
        SockConnection destination = s.getPeerConnection(sourceConnection);
        destination.sendMessage(getRawMessage());
        log.info("Session {}: Sent error message '{}' to {}", s.getSessionNr(), errMessage, destination.getClientName());
    }
}
