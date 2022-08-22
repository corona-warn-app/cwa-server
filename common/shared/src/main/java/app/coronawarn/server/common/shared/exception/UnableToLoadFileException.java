package app.coronawarn.server.common.shared.exception;

/**
 * The file could not be loaded/parsed correctly.
 */
public class UnableToLoadFileException extends Exception {
  private static final long serialVersionUID = 5554527058338857525L;

  public UnableToLoadFileException(String path) {
    super("Unable to load file from path " + path);
  }

  public UnableToLoadFileException(String message, Throwable cause) {
    super(message, cause);
  }
}
