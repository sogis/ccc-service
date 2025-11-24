package ch.so.agi.cccservice.exception;

/**
 * Base class for all exceptions that can be triggered by invalid
 * client behaviour. Each client exception provides an error code and
 * message that can be forwarded to the client as a notifyError message.
 */
public abstract class ClientException extends RuntimeException {
    private final int code;

    protected ClientException(int code, String exMessage) {
        this(code, exMessage, null);
    }

    protected ClientException(int code, String exMessage, Throwable cause) {
        super(exMessage, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
