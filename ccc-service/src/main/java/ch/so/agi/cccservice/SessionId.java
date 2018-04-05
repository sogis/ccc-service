package ch.so.agi.cccservice;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SessionId, which will be used to connect two WebSocketSessions
 */
public class SessionId {

    private String sessionId;

    /**
     * Constructor
     * @param id SessionId as String
     */
    public SessionId (String id) {
        sessionId = id;
    }

    /**
     * Compares if two SessionId are the same
     * @param o SessionId
     * @return true/false if they are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionId sessionId1 = (SessionId) o;
        return Objects.equals(sessionId, sessionId1.sessionId);
    }

    /**
     * Generates hashCode of sessionId
     * @return hashCode
     */
    @Override
    public int hashCode() {

        return Objects.hash(sessionId);
    }

    /**
     * Converts SessionId to a message specific String
     * @return SessionId as String
     */
    @Override
    public String toString() {
        return "SessionId{" +
                "sessionId='" + sessionId + '\'' +
                '}';
    }

    /**
     * Gets SessionId
     * @return SessionId as String
     */
    @JsonProperty("session")
    public String getSessionId() {
        return sessionId;
    }



}
