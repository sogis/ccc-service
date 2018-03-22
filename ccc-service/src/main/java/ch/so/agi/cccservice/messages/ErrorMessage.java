package ch.so.agi.cccservice.messages;

/**
 *
 */
public class ErrorMessage extends AbstractMessage{
    private int code;
    private String message;

    public ErrorMessage(int code, String message) {
        super("error");
        this.code = code;
        this.message = message;
    }


    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
