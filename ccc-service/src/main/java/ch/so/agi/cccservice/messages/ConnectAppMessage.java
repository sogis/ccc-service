package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import ch.so.agi.cccservice.SessionId;

/**
 * Message which will be send from the application to start connection
 */
public class ConnectAppMessage extends AbstractMessage {

    public static final String METHOD_NAME = "connectApp";

    @JsonUnwrapped
    private SessionId session;
    private String clientName;
    private String apiVersion;

    /**
     * Constructor
     */
    public ConnectAppMessage() {
        super(METHOD_NAME);
    }


    /**
     * Gets SessionId delivered in message
     * @return SessionId
     */
    public SessionId getSession() {
        return session;
    }

    /**
     * Sets SessionId for message
     * @param session as SessionId
     */
    public void setSession(SessionId session) {
        this.session = session;
    }

    /**
     * Gets clientName delivered in message
     * @return clientName as String
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Sets clientName for message
     * @param clientName as String
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * Gets apiVersion delivered in message
     * @return apiVersion as String
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Sets apiVersion for message
     * @param apiVersion as String
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
