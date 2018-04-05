package ch.so.agi.cccservice;

/**
 * Exception which will be thrown on specific CCC-Server-Errors
 */
public class ServiceException extends Exception{

    private int errorCode;

    /**
     * Constructor
     * @param errorCode of error
     * @param errorMessage to specify error
     */
    public ServiceException(int errorCode, String errorMessage){
        super(errorMessage);
        this.errorCode = errorCode;
    }

    /**
     * Gets ErrorCode of ServiceException
     * @return errorCode
     */
    public int getErrorCode() {
        return errorCode;
    }
}
