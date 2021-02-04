package app.coronawarn.server.services.distribution.statistics.exceptions;

@SuppressWarnings("serial")
public class FilePathNotFoundException extends RuntimeException {
  public FilePathNotFoundException(final String path, final Throwable cause) {
    super(String.format("Failed to load file at path: %s", path), cause);
  }
}
