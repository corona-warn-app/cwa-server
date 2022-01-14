package app.coronawarn.server.common.shared.functional;

public class CheckedFunctionAndConsumerException extends RuntimeException {

  public CheckedFunctionAndConsumerException(final Throwable cause) {
    super(cause);
  }
}
