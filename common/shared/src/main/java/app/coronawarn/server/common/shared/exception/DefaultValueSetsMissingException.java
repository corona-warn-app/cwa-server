package app.coronawarn.server.common.shared.exception;

import java.io.IOException;

public class DefaultValueSetsMissingException extends Exception {

  public DefaultValueSetsMissingException(String message, IOException exception) {
    super(message,exception);
  }

}
