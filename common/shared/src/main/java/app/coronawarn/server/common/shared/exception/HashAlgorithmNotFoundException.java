package app.coronawarn.server.common.shared.exception;

public class HashAlgorithmNotFoundException extends RuntimeException {

  public HashAlgorithmNotFoundException(String msg, Throwable rootCause) {
    super(msg, rootCause);
  }
}
