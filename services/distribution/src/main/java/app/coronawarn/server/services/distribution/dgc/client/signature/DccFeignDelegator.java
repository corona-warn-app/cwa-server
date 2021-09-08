package app.coronawarn.server.services.distribution.dgc.client.signature;

import feign.Client;
import feign.Request;
import feign.Request.Options;
import feign.Response;
import feign.httpclient.ApacheHttpClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class DccFeignDelegator implements Client {

  public static final String X_SIGNATURE = "X-SIGNATURE";

  private final ApacheHttpClient apacheHttpClient;
  private final DccSignatureValidator dccSignatureValidator;

  public DccFeignDelegator(ApacheHttpClient apacheHttpClient, DccSignatureValidator dccSignatureValidator) {
    this.apacheHttpClient = apacheHttpClient;
    this.dccSignatureValidator = dccSignatureValidator;
  }

  @Override
  public Response execute(Request request, Options options) throws IOException {
    Response response = apacheHttpClient.execute(request, options);
    String body = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
    String signature = getSignature(response, request.url());

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
   * @param response - response.
   * @param requestUrl - request url.
   * @return - signature found on 'x-signature' headers key.
   * @throws IOException - thrown if 'x-signature' header is missing.
   */
  private String getSignature(Response response, String requestUrl) throws IOException {
    Collection<String> header = response.headers().get(X_SIGNATURE);
    if (header == null || header.isEmpty()) {
      throw new IOException(X_SIGNATURE + " header is missing from the response of: " + requestUrl);
    }
    return header.iterator().next();
  }


}
