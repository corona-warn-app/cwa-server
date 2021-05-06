package app.coronawarn.server.services.distribution.dgc;

import java.io.IOException;

public class DefaultValuesetsMissingException extends Exception {


  public DefaultValuesetsMissingException(String message, IOException exception) {
    super(message,exception);
  }
}
