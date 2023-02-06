package app.coronawarn.server.services.distribution.dgc.client.signature;

import feign.Client;
import feign.Request;
import feign.Request.Options;
import feign.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class DccFeignDelegator implements Client {

  public static final String X_SIGNATURE = "X-SIGNATURE";

  private final Client feignClient;

  private final DccSignatureValidator dccSignatureValidator;

  public DccFeignDelegator(final Client feignClient, final DccSignatureValidator dccSignatureValidator) {
    this.feignClient = feignClient;
    this.dccSignatureValidator = dccSignatureValidator;
  }

  @Override
  public Response execute(final Request request, final Options options) throws IOException {
    final Response response = feignClient.execute(request, options);
    final String body = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
    final String signature = getSignature(response, request.url());

    dccSignatureValidator.checkSignature(signature, body);

    // response recreated to avoid closed stream exception.
    return Response.builder()
        .status(response.status())
        .reason(response.reason())
        .headers(response.headers())
        .request(response.request())
        .body(body, StandardCharsets.UTF_8).build();
  }

  /**
   * Get the signature from the HTTP response headers.
   *
   * @param response   - response.
   * @param requestUrl - request url.
   * @return - signature found on 'x-signature' headers key.
   * @throws IOException - thrown if 'x-signature' header is missing.
   */
  private String getSignature(final Response response, final String requestUrl) throws IOException {
    final Collection<String> header = response.headers().get(X_SIGNATURE);
    if (header == null || header.isEmpty()) {
      throw new IOException(X_SIGNATURE + " header is missing from the response of: " + requestUrl);
    }
    return header.iterator().next();
  }
}
