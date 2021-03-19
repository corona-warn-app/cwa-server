package app.coronawarn.server.services.eventregistration.boundary;

import javax.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class TraceLocationApiErrorHandler {


  @ExceptionHandler(value = {ConstraintViolationException.class})
  public ResponseEntity<Void> handleConstraintViolationExceptions(RuntimeException e, WebRequest request) {
    return ResponseEntity.badRequest().build();
  }

}
