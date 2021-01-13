package app.coronawarn.server.services.callback;

import org.springframework.security.core.AuthenticationException;

public class EfgsCertificateCnException extends AuthenticationException {

  public EfgsCertificateCnException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public EfgsCertificateCnException(String msg) {
    super(msg);

  }
}
