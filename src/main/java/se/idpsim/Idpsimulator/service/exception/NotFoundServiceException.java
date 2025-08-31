package se.idpsim.Idpsimulator.service.exception;

public class NotFoundServiceException extends RuntimeException {
    public NotFoundServiceException(String message) {
        super(message);
    }
    public NotFoundServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
