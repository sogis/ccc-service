package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Message sent from the ccc-server to either domain-application or GIS to notify an error.
 */
public class NotifyErrorMessage extends AbstractMessage{

    public static final String METHOD_NAME = "notifyError";
    private int code;
    private String message;
    private JsonNode userData;
    private String nativeCode;
    private String technicalDetails;

    /**
     * Constructor
     */
    public NotifyErrorMessage() {
        super(METHOD_NAME);
    }

    /**
     * Gets errorCode of message
     * @return code as int
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets errormessage of message
     * @return message as String
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets UserDate which contains details about where error happened
     * @return userData as Json
     */
    public JsonNode getUserData() {
        return userData;
    }

    /**
     * Gets code of error in specific endpoint
     * @return nativeCode as String
     */
    public String getNativeCode() {
        return nativeCode;
    }

    /**
     * Gets additional information on error
     * @return technicalDetails as String
     */
    public String getTechnicalDetails() {
        return technicalDetails;
    }

    /**
     * sets errorCode in message
     * @param code as int
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * sets errorMessage in message
     * @param message as String
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets UserDate which contains details about where error happened
     * @param userData as Json
     */
    public void setUserData(JsonNode userData) {
        this.userData = userData;
    }

    /**
     * Sets code of error in specific endpoint
     * @param nativeCode as String
     */
    public void setNativeCode(String nativeCode) {
        this.nativeCode = nativeCode;
    }

    /**
     * Sets additional information on error
     * @param technicalDetails as String
     */
    public void setTechnicalDetails(String technicalDetails){
        this.technicalDetails = technicalDetails;
    }
}
