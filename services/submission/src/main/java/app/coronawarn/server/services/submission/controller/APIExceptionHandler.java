package app.coronawarn.server.services.submission.controller;

import com.google.protobuf.InvalidProtocolBufferException;
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
public class APIExceptionHandler {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public void unknownException(Exception ex, WebRequest wr) {
    logger.error("Unable to handle {}", wr, ex);
  }

  @ExceptionHandler({HttpMessageNotReadableException.class, ServletRequestBindingException.class,
      InvalidProtocolBufferException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void bindingExceptions(Exception ex, WebRequest wr) {
    logger.error("Binding failed {}", wr, ex);
  }

}
