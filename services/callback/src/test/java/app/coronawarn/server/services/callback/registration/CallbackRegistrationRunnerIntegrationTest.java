package app.coronawarn.server.services.callback.registration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import app.coronawarn.server.common.federation.client.callback.RegistrationResponse;
import app.coronawarn.server.common.shared.util.HashUtils;
import app.coronawarn.server.services.callback.config.CallbackServiceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"callback-registration"})
@DirtiesContext
class CallbackRegistrationRunnerIntegrationTest {

  private static WireMockServer server;

  @SpyBean
  private CallbackServiceConfig callbackServiceConfig;

  @BeforeAll
  static void setupWireMock() {
    RegistrationResponse registrationResponse1 = new RegistrationResponse(HashUtils.md5DigestAsHex("url1"), "url1");
    List<RegistrationResponse> responses = List.of(registrationResponse1);

    server = new WireMockServer(options().port(1234));
    server.start();
    server.stubFor(
        get(urlEqualTo("/diagnosiskeys/callback"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(getHttpHeaders())
                    .withBody(asJsonString(responses))));

    server.stubFor(
        put(urlEqualTo("/diagnosiskeys/callback/" + HashUtils.md5DigestAsHex("url") + "?url=url"))
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
  void testRegistration() {
    String expectedGetUrl = "/diagnosiskeys/callback";
    server.verify(1, getRequestedFor(urlEqualTo(expectedGetUrl)));

    String expectedPutUrl = "/diagnosiskeys/callback/"
        + HashUtils.md5DigestAsHex("url") + "?url=url";
    server.verify(1, putRequestedFor(urlEqualTo(expectedPutUrl)));

    verify(callbackServiceConfig, times(1)).isRegisterOnStartup();
    verify(callbackServiceConfig, times(1)).getEndpointUrl();
  }
}
