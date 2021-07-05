

package app.coronawarn.server.common.shared.exception;

/**
 * The file could not be loaded/parsed correctly.
 */
public class UnableToLoadFileException extends Exception {

  public UnableToLoadFileException(String path) {
    super("Unable to load file from path " + path);
  }

  public UnableToLoadFileException(String message, Throwable cause) {
    super(message, cause);
  }
}
