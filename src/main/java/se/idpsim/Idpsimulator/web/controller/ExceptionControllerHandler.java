package se.idpsim.Idpsimulator.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import se.idpsim.Idpsimulator.service.exception.BadInputServiceException;

@ControllerAdvice
@Slf4j
public class ExceptionControllerHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception e) {
        ProblemDetail pb = switch(e) {
            case NoResourceFoundException ignored ->
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
            case IllegalArgumentException ignored ->
                 ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
            case BadInputServiceException ignored ->
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
            default ->
                ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        };
        logError(e);
        return ResponseEntity.status(pb.getStatus()).body(pb);
    }

    private void logError(Exception e) {
        switch (e) {
            case NoResourceFoundException ignored -> {}
            default -> log.error("Unexpected exception: ", e);
        }
    }
}
