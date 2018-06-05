package ch.so.agi.cccservice.messages;

/**
 * Root of all classes that implement ccc-messages.
 */
abstract public class AbstractMessage {

    private String method;

    /**
     * Constructor
     * @param method ccc-method-name
     */
    public AbstractMessage(String method) {
        this.method = method;
    }

    /**
     * gets the ccc-method-name of message.
     * @return ccc-method-name
     */
    public String getMethod() {
        return method;
    }

}
