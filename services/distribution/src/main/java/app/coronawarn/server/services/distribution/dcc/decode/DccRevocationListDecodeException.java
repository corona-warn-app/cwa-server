package app.coronawarn.server.services.distribution.dcc.decode;

public class DccRevocationListDecodeException extends Exception {
  private static final long serialVersionUID = 773876244555365963L;

  public DccRevocationListDecodeException(String message, Throwable cause) {
    super(message, cause);
  }
}
