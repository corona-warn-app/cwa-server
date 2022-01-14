package app.coronawarn.server.services.distribution.statistics.file;

public class JsonFileLoaderException extends RuntimeException {

  public JsonFileLoaderException(String path, final Throwable cause) {
    super(String.format("Failed to load Local JSON from path %s", path), cause);
  }
}
