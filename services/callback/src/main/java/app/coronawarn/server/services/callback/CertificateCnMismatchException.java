package app.coronawarn.server.services.callback;

import org.springframework.security.core.AuthenticationException;

public class CertificateCnMismatchException extends AuthenticationException {
  private static final long serialVersionUID = 5480042926779226129L;

  public CertificateCnMismatchException(String msg) {
    super(msg);
  }
}
