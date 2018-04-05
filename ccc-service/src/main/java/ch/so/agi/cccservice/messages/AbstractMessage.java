package ch.so.agi.cccservice.messages;

/**
 * AbstractMessage-Class which provides a basis for all Message-classes
 */
abstract public class AbstractMessage {

    private String method;

    /**
     * Constructor
     * @param method as String
     */
    public AbstractMessage(String method) {
        this.method = method;
    }

    /**
     * Gets method-properties of message
     * @return method as string
     */
    public String getMethod() {
        return method;
    }

}
