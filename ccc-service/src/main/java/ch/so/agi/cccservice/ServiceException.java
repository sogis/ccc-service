package ch.so.agi.cccservice;

public class ServiceException extends Exception{

    private int errorCode;

    public ServiceException(int errorCode, String errorMessage){
        super(errorMessage);
        this.errorCode = errorCode;
    }


    public int getErrorCode() {
        return errorCode;
    }
}
