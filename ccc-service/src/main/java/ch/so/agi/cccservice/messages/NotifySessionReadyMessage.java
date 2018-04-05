package ch.so.agi.cccservice.messages;

/**
 * Message sent from CCC-Server to Application and GIS on sucessfull connection between them
 */
public class NotifySessionReadyMessage extends AbstractMessage {
    public static final String METHOD_NAME = "notifySessionReady";
    private String apiVersion;

    /**
     * Constructor
     */
    public NotifySessionReadyMessage() {
        super(METHOD_NAME);
    }

    /**
     * Gets apiVersion
     * @return apiVersion as String
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Sets apiVersion in message
     * @param apiVersion as String
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
