package ch.so.agi.cccservice;

import java.util.Objects;

public class SessionId {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionId sessionId1 = (SessionId) o;
        return Objects.equals(sessionId, sessionId1.sessionId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "SessionId{" +
                "sessionId='" + sessionId + '\'' +
                '}';
    }

    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public SessionId (String id) {
        sessionId = id;
    }

}
