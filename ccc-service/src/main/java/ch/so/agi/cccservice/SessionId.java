package ch.so.agi.cccservice;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class SessionId {

    private String sessionId;


    public SessionId (String id) {
        sessionId = id;
    }


    public String getSessionId() {
        return sessionId;
    }

    @JsonProperty("session")
    public String sessionId() {
        return sessionId.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SessionId sessionId1 = (SessionId) o;

        return Objects.equals(sessionId, sessionId1.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return sessionId ;
    }









}
