package app.coronawarn.server.common.federation.client;

public class CloudFeignHttpClientProviderException extends RuntimeException {
  private static final long serialVersionUID = 8199713110526635715L;

  public CloudFeignHttpClientProviderException(final Throwable cause) {
    super(cause);
  }
}
