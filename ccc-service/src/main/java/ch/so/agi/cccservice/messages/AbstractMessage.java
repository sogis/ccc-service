package ch.so.agi.cccservice.messages;

abstract public class AbstractMessage {

    public AbstractMessage(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    private String method;
}
