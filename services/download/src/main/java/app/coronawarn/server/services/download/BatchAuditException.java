package app.coronawarn.server.services.download;

public class BatchAuditException extends RuntimeException {

  public BatchAuditException(String msg) {
    super(msg);
  }

  public BatchAuditException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
