package app.coronawarn.server.services.distribution.statistics.exceptions;

public class FilePathNotFoundException extends RuntimeException {

  public FilePathNotFoundException(String path) {
    super(String.format("Failed to load file at path: %s", path));
  }

}
