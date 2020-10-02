

package app.coronawarn.server.services.callback.controller;

import java.net.URI;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * RequestExecutor executes requests against the diagnosis key callback endpoint for testing purposes.
 */
@Component
public class RequestExecutor {

  private static final URI CALLBACK_URL = URI.create("/version/v1/callback");

  private final TestRestTemplate testRestTemplate;

  public RequestExecutor(TestRestTemplate testRestTemplate) {
    this.testRestTemplate = testRestTemplate;
  }

  public ResponseEntity<Void> executeGet(String batchTag, String dateString) {
    return testRestTemplate.getForEntity(buildRequestUrl(batchTag, dateString), Void.class);
  }

  private String buildRequestUrl(String batchTag, String dateString) {
    StringBuilder requestUrl = new StringBuilder();
    requestUrl.append(CALLBACK_URL);

    if (batchTag != null || dateString != null) {
      requestUrl.append("?");
    }
    if (batchTag != null) {
      requestUrl.append("batchTag=" + batchTag);
    }
    if (batchTag != null && dateString != null) {
      requestUrl.append("&");
    }
    if (dateString != null) {
      requestUrl.append("date=" + dateString);
    }

    return requestUrl.toString();
  }
}
