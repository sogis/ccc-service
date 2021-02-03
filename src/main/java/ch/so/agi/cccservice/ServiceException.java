package ch.so.agi.cccservice;

/**
 * Any exception thrown by the ccc-server.
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
