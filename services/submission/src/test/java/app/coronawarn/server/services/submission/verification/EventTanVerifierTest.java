
package app.coronawarn.server.services.submission.verification;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import feign.FeignException;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
class EventTanVerifierTest {

  @Autowired
  private EventTanVerifier eventTanVerifier;

  @Autowired
  private SubmissionServiceConfig submissionServiceConfig;

  @MockBean
  TestRestTemplate testRestTemplate;

  private String verificationPath;
  private String randomUUID;
  private static WireMockServer server;

  @BeforeAll
  static void setupWireMock() {
    server = new WireMockServer(options().port(1234));
    server.start();
  }

  @BeforeEach
  void setup() {
    this.verificationPath = submissionServiceConfig.getVerificationPath();
    this.randomUUID = UUID.randomUUID().toString();
    server.resetAll();
  }

  @AfterAll
  static void tearDown() {
    server.stop();
  }

  @Test
  void checkValidEventTan() {
    server.stubFor(
        post(urlEqualTo(verificationPath))
            .withRequestBody(matchingJsonPath("tan", equalTo(randomUUID)))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader(TanVerificationService.CWA_TELETAN_TYPE_RESPONSE_HEADER,
                    TanVerificationService.CWA_TELETAN_TYPE_EVENT)));

    boolean tanVerificationResponse = eventTanVerifier.verifyTan(randomUUID);

    assertThat(tanVerificationResponse).isTrue();
  }

  @Test
  void checkInvalidTan() {
    server.stubFor(
        post(urlEqualTo(verificationPath)).withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

    boolean tanVerificationResponse = eventTanVerifier.verifyTan(randomUUID);

    assertThat(tanVerificationResponse).isFalse();
  }

  @Test
  void checkTooLongTan() {
    server.stubFor(
        post(urlEqualTo(verificationPath)).withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

    boolean tanVerificationResponse = eventTanVerifier.verifyTan(randomUUID + randomUUID);

    assertThat(tanVerificationResponse).isFalse();
  }

  @Test
  void checkInternalServerError() {
    server.stubFor(
        post(urlEqualTo(verificationPath)).withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

    assertThatExceptionOfType(FeignException.class).isThrownBy(() -> eventTanVerifier.verifyTan(randomUUID));
  }

  @Test
  void checkTimeout() {
    server.stubFor(
        post(urlEqualTo(verificationPath))
            .withRequestBody(matchingJsonPath("tan", equalTo(randomUUID)))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withFixedDelay(1000)));

    assertThatExceptionOfType(FeignException.class).isThrownBy(() -> eventTanVerifier.verifyTan(randomUUID));
  }

  @Test
  void checkRegularTanFails() {
    server.stubFor(
        post(urlEqualTo(verificationPath))
            .withRequestBody(matchingJsonPath("tan", equalTo(randomUUID)))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.OK.value())));

    boolean tanVerificationResponse = eventTanVerifier.verifyTan(randomUUID);

    assertThat(tanVerificationResponse).isFalse();
  }
}
