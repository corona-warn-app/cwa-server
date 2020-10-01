

package app.coronawarn.server.services.submission.controller;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import com.google.protobuf.InvalidProtocolBufferException;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice("app.coronawarn.server.services.submission.controller")
public class ApiExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public void unknownException(Exception ex, WebRequest wr) {
    logger.error("Unable to handle {}", getFormattedDescription(wr), ex);
  }

  @ExceptionHandler({HttpMessageNotReadableException.class, ServletRequestBindingException.class,
      InvalidProtocolBufferException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void bindingExceptions(Exception ex, WebRequest wr) {
    logger.error("Binding failed {}", getFormattedDescription(wr), ex);
  }

  @ExceptionHandler({
      InvalidDiagnosisKeyException.class, ConstraintViolationException.class, IllegalArgumentException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void diagnosisKeyExceptions(Exception ex, WebRequest wr) {
    logger.error("Erroneous Submission Payload {}", getFormattedDescription(wr), ex);
  }

  private String getFormattedDescription(WebRequest wr) {
    return wr.getDescription(false).replaceAll("[\n|\r|\t]", "_");
  }
}
