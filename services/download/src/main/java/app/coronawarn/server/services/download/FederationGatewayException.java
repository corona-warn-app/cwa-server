

package app.coronawarn.server.services.download;

public class FederationGatewayException extends RuntimeException {

  public FederationGatewayException(String msg) {
    super(msg);
  }

  public FederationGatewayException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
