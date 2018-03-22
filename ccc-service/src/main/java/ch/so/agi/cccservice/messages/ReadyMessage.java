package ch.so.agi.cccservice.messages;

public class ReadyMessage extends AbstractMessage {
    private String apiVersion;

    public ReadyMessage() {
        super("ready");
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
