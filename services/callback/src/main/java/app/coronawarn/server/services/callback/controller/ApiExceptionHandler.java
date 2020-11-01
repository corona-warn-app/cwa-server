

package app.coronawarn.server.services.callback.controller;

import java.text.ParseException;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice("app.coronawarn.server.services.callback.controller")
public class ApiExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public void unknownException(Exception ex, WebRequest wr) {
    logger.error("Unable to handle {}", wr.getDescription(false), ex);
  }

  @ExceptionHandler({ConstraintViolationException.class, ParseException.class,
      MissingServletRequestParameterException.class, TypeMismatchException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void handleConstraintViolationException(Exception ex, WebRequest wr) {
    logger.error("Erroneous callback url call {}", wr.getDescription(false), ex);
  }

}
