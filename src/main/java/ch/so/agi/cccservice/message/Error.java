package ch.so.agi.cccservice.message;

import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.WebSocketSession;

import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Error extends Message {

    public static final String MESSAGE_TYPE = "notifyError";

    @JsonProperty("code")
    @NotNull
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
    public Error() { super(MESSAGE_TYPE); }

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
        s.getPeerConnection(sourceConnection).sendMessage(getRawMessage());

        String source = "app";
        String destination = "gis";
        if(!sourceConnection.equals(s.getAppWebSocket())){
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
