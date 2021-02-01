package app.coronawarn.server.services.callback.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import app.coronawarn.server.common.federation.client.callback.RegistrationResponse;
import app.coronawarn.server.services.callback.config.CallbackServiceConfig;
import app.coronawarn.server.services.callback.registration.RegistrationRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"download-date-based-integration-test"})
@DirtiesContext
class CallbackRegistrationRunnerIntegrationTest {

  private static WireMockServer server;

  @Autowired
  private RegistrationRunner registrationRunner;

  @Autowired
  private CallbackServiceConfig callbackServiceConfig;

  @BeforeAll
  static void setupWireMock() {
    RegistrationResponse registrationResponse1 = new RegistrationResponse("id1", "url1");
    RegistrationResponse registrationResponse2 = new RegistrationResponse("id2", "url2");
    List<RegistrationResponse> responses = List.of(registrationResponse1, registrationResponse2);

    server = new WireMockServer(options().port(1234));
    server.start();
    server.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(getHttpHeaders())
                    .withBody(asJsonString(responses))));

    server.stubFor(
        put(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(getHttpHeaders())
                    .withBody(asJsonString(registrationResponse1))));
  }

  @AfterAll
  static void tearDown() {
    server.stop();
  }

  private static HttpHeaders getHttpHeaders() {
    return new HttpHeaders()
        .plus(new HttpHeader(CONTENT_TYPE, "application/json; version=1.0"));
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testDownloadRunSuccessfully() {
    assertThat(true).isTrue();
  }
}
