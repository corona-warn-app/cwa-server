package app.coronawarn.server.services.distribution.statistics.file;

public class JsonFileLoaderException extends RuntimeException {
  private static final long serialVersionUID = 481244982371451315L;

  public JsonFileLoaderException(String path, final Throwable cause) {
    super(String.format("Failed to load Local JSON from path %s", path), cause);
  }
}
