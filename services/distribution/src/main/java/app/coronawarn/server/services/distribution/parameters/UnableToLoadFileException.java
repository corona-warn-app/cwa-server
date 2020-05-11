package app.coronawarn.server.services.distribution.parameters;

/**
 * The file could not be loaded/parsed correctly.
 */
public class UnableToLoadFileException extends Exception {

  public UnableToLoadFileException(String message) {
    super(message);
  }

  public UnableToLoadFileException(String message, Throwable cause) {
    super(message, cause);
  }
}
