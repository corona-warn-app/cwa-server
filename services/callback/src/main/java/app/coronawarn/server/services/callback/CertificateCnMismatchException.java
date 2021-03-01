package app.coronawarn.server.services.callback;

import org.springframework.security.core.AuthenticationException;

public class CertificateCnMismatchException extends AuthenticationException {

  public CertificateCnMismatchException(String msg) {
    super(msg);
  }
}
