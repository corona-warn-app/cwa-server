package app.coronawarn.server.services.download;

public class FatalFederationGatewayException extends Exception {
  private static final long serialVersionUID = -7256623337128544306L;

  public FatalFederationGatewayException(String message) {
    super(message);
  }
}
