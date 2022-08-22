package app.coronawarn.server.common.shared.functional;

public class CheckedFunctionAndConsumerException extends RuntimeException {
  private static final long serialVersionUID = -7637713928805965646L;

  public CheckedFunctionAndConsumerException(final Throwable cause) {
    super(cause);
  }
}
