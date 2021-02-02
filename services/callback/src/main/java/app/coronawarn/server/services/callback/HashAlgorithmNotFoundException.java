package app.coronawarn.server.services.callback;

public class HashAlgorithmNotFoundException extends RuntimeException {

  public HashAlgorithmNotFoundException(String msg) {
    super(msg);
  }
}
