package app.coronawarn.server.services.callback;

public class HashAlgorithmNotFoundException extends RuntimeException {

  public HashAlgorithmNotFoundException(String msg, Throwable rootCause) {
    super(msg, rootCause);
  }
}
