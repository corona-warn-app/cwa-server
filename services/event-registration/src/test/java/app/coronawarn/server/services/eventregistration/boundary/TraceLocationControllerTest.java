package app.coronawarn.server.services.eventregistration.boundary;

import static app.coronawarn.server.services.eventregistration.config.UrlConstants.TRACE_LOCATION_ROUTE;
import static app.coronawarn.server.services.eventregistration.config.UrlConstants.V1;
import static app.coronawarn.server.services.eventregistration.testdata.TestData.traceLocation;

import app.coronawarn.server.common.protocols.internal.pt.TraceLocation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TraceLocationControllerTest {

  @Autowired
  private TestRestTemplate template;


  @Test
  public void shouldReturnNoContent() throws Exception {
    TraceLocation payload = traceLocation()
        .withDescription("description")
        .withStartTimestamp(10)
        .withEndTimestamp(100)
        .withEmptyGuid()
        .withAddress("address")
        .withVersion(5)
        .withDefaultCheckInLength(5).build();
    HttpHeaders headers = new HttpHeaders();
    headers.set("content-type", "application/x-protobuf");

    HttpEntity<TraceLocation> request = new HttpEntity<>(payload, headers);
    final ResponseEntity<String> response = this.template
        .postForEntity(V1 + TRACE_LOCATION_ROUTE, request, String.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

}
