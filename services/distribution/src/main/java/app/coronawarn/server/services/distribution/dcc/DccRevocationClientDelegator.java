package app.coronawarn.server.services.distribution.dcc;

import feign.Client;
import feign.Request;
import feign.Request.Options;
import feign.Response;
import feign.Response.Body;
import feign.httpclient.ApacheHttpClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DccRevocationClientDelegator implements Client {

  private static final Logger logger = LoggerFactory.getLogger(DccRevocationClientDelegator.class);

  private final ApacheHttpClient apacheHttpClient;

  public DccRevocationClientDelegator(final ApacheHttpClient apacheHttpClient) {
    this.apacheHttpClient = apacheHttpClient;
  }

  @Override
  public Response execute(final Request request, final Options options) throws IOException {
    final Response response = apacheHttpClient.execute(request, options);

    // in case of http HEAD the response is NULL!
    final Body body = response.body();
    if (body != null) {
      return response;
    } else {
      logger.info("response body is null for '{}'", request);
    }

    return Response.builder()
        .status(response.status())
        .reason(response.reason())
        .headers(response.headers())
        .request(response.request())
        .body("", StandardCharsets.UTF_8).build();
  }
}
