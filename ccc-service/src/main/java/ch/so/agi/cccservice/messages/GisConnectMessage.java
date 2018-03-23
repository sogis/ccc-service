package ch.so.agi.cccservice.messages;

import ch.so.agi.cccservice.SessionId;

public class GisConnectMessage extends AbstractMessage {
    private SessionId session;
    private String clientName;
    private String apiVersion;

    public GisConnectMessage() {
        super("handleGisConnect");
    }

    public SessionId getSession() {
        return session;
    }

    public void setSession(SessionId session) {
        this.session = session;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
