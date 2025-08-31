package se.idpsim.Idpsimulator.service.exception;

public class BadInputServiceException extends RuntimeException {
    public BadInputServiceException(String message) {
        super(message);
    }
    public BadInputServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
