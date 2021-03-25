package app.coronawarn.server.services.eventregistration.boundary;

import app.coronawarn.server.services.eventregistration.domain.errors.SigningException;
import app.coronawarn.server.services.eventregistration.domain.errors.TraceLocationInsertionException;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class TraceLocationApiErrorHandler {

  private static final Logger logger = LoggerFactory.getLogger(TraceLocationApiErrorHandler.class);


  @ExceptionHandler(value = {ConstraintViolationException.class})
  public ResponseEntity<Void> handleConstraintViolationExceptions(ConstraintViolationException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
  }

  @ExceptionHandler(value = {SigningException.class})
  public ResponseEntity<Void> handleSigningException(RuntimeException e, WebRequest request) {
    logger.error(e.getMessage(), e.getCause());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }

  @ExceptionHandler(value = {TraceLocationInsertionException.class})
  public ResponseEntity<Void> handleInsertionOfTraceLocationFailed(RuntimeException e, WebRequest request) {
    logger.error(e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }

}
