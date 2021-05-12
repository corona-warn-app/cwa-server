package app.coronawarn.server.services.distribution.dgc;

import java.io.IOException;

public class DefaultValueSetsMissingException extends Exception {

  public DefaultValueSetsMissingException(String message, IOException exception) {
    super(message,exception);
  }

}
