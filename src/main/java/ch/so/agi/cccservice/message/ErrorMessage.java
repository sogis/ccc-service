package ch.so.agi.cccservice.message;

import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorMessage extends Message {

    public static final String MESSAGE_TYPE = "notifyError";

    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    @NotNull
    private String errMessage;
    @JsonProperty("userData")
    private JsonNode userData;
    @JsonProperty("nativeCode")
    private String nativeCode;
    @JsonProperty("technicalDetails")
    private String technicalDetails;

    // Required no-args constructor
    public ErrorMessage() { super(MESSAGE_TYPE); }

    public String getTechnicalDetails() {
        return technicalDetails;
    }

    public String getNativeCode() {
        return nativeCode;
    }

    public JsonNode getUserData() {
        return userData;
    }

    public int getCode() {
        return code;
    }

    public String getErrMessage() {
        return errMessage;
    }

    @Override
    public void process(WebSocketSession sourceConnection) {
        Session s = Sessions.findByConnection(sourceConnection);
        s.assertConnected(this);
        boolean destIsApp = !sourceConnection.equals(s.getAppWebSocket());
        String msg = destIsApp ? rawMessageForApp(s.getAppConnection()) : getRawMessage();
        s.getPeerConnection(sourceConnection).sendMessage(msg);

        String source = "app";
        String destination = "gis";
        if (destIsApp) {
            source = "gis";
            destination = "app";
        }

        log.info(
                "Session {}: Sent error from {} to {}. Errorcode: {}. Errormessage: {}",
                s.getSessionNr(),
                source,
                destination,
                getCode(),
                getErrMessage()
        );
    }
}
