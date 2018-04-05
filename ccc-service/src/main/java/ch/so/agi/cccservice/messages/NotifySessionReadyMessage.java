package ch.so.agi.cccservice.messages;

public class NotifySessionReadyMessage extends AbstractMessage {
    public static final String METHOD_NAME = "notifySessionReady";
    private String apiVersion;

    public NotifySessionReadyMessage() {
        super(METHOD_NAME);
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
