package ch.so.agi.service.exception;

/**
 * Base class for all exceptions that can be triggered by invalid
 * client behaviour. Each client exception provides an error code and
 * message that can be forwarded to the client as a notifyError message.
 */
public abstract class ClientException extends RuntimeException {
    private final int code;
    private final String clientMessage;
    private final String technicalDetails;

    protected ClientException(int code, String clientMessage) {
        this(code, clientMessage, null, null);
    }

    protected ClientException(int code, String clientMessage, String technicalDetails) {
        this(code, clientMessage, technicalDetails, null);
    }

    protected ClientException(int code, String clientMessage, String technicalDetails, Throwable cause) {
        super(clientMessage, cause);
        this.code = code;
        this.clientMessage = clientMessage;
        this.technicalDetails = technicalDetails;
    }

    public int getCode() {
        return code;
    }

    public String getClientMessage() {
        return clientMessage;
    }

    public String getTechnicalDetails() {
        return technicalDetails;
    }
}
