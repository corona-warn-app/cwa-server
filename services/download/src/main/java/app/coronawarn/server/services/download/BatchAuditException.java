package app.coronawarn.server.services.download;

public class BatchAuditException extends RuntimeException {
  private static final long serialVersionUID = 421314018302400787L;

  public BatchAuditException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
