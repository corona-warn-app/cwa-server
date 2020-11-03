package app.coronawarn.server.services.download;

public class BatchDownloadException extends RuntimeException {

  public BatchDownloadException(String msg) {
    super(msg);
  }

  public BatchDownloadException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
