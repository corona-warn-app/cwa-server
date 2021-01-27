package app.coronawarn.server.services.distribution.statistics.exceptions;

public class NotModifiedException extends Exception {
  public NotModifiedException(String path, String etag) {
    super(String.format("File %s not modified since last checked. ETag: %s", path, etag));
  }
}
