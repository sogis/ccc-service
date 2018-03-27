package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 */
public class ErrorMessage extends AbstractMessage{
    private int code;
    private String message;
    private JsonNode userData;
    private String nativeCode;
    private String technicalDetails;

    public ErrorMessage() {
        super("error");
    }


    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public JsonNode getUserData() {
        return userData;
    }

    public String getNativeCode() {
        return nativeCode;
    }

    public String getTechnicalDetails() {
        return technicalDetails;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUserData(JsonNode userData) {
        this.userData = userData;
    }

    public void setNativeCode(String nativeCode) {
        this.nativeCode = nativeCode;
    }

    public void setTechnicalDetails(String technicalDetails){
        this.technicalDetails = technicalDetails;
    }
}
