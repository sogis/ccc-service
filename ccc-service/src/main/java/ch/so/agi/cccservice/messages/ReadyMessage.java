package ch.so.agi.cccservice.messages;

public class ReadyMessage extends AbstractMessage {
    public static final String READY = "ready";
    private String apiVersion;

    public ReadyMessage() {
        super(READY);
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
