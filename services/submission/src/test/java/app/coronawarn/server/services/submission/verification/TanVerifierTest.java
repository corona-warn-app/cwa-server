
package app.coronawarn.server.services.submission.verification;

import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import feign.FeignException;
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
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@SpringBootTest
@DirtiesContext
class TanVerifierTest {

  @Autowired
  private TanVerifier underTest;

  @Autowired
  private SubmissionServiceConfig submissionServiceConfig;

  @MockBean
  TestRestTemplate testRestTemplate;

  private String verificationPath;
  private String randomUUID;

  private static WireMockServer server = new WireMockServer(options().port(1234));

  @BeforeAll
  static void setupWireMock() {
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
  void checkValidTan() {
    server.stubFor(
        post(urlEqualTo(verificationPath))
            .withRequestBody(matchingJsonPath("tan", equalTo(randomUUID)))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(ok()));

    boolean tanVerificationResponse = underTest.verifyTan(randomUUID);

    assertThat(tanVerificationResponse).isTrue();
  }

  @Test
  void checkInvalidTan() {
    server.stubFor(
        post(urlEqualTo(verificationPath)).withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(notFound()));

    boolean tanVerificationResponse = underTest.verifyTan(randomUUID);

    assertThat(tanVerificationResponse).isFalse();
  }

  @Test
  void checkTooLongTan() {
    server.stubFor(
        post(urlEqualTo(verificationPath)).withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(notFound()));

    boolean tanVerificationResponse = underTest.verifyTan(randomUUID + randomUUID);

    assertThat(tanVerificationResponse).isFalse();
  }

  @Test
  void checkEventTanFails() {
    server.stubFor(
        post(urlEqualTo(verificationPath))
            .withRequestBody(matchingJsonPath("tan", equalTo(randomUUID)))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(ok()
                .withHeader(EventTanVerifier.CWA_TELETAN_TYPE_RESPONSE_HEADER,
                    EventTanVerifier.CWA_TELETAN_TYPE_EVENT)));

    boolean tanVerificationResponse = underTest.verifyTan(randomUUID);

    assertThat(tanVerificationResponse).isFalse();
  }

  @Test
  void checkInternalServerError() {
    server.stubFor(
        post(urlEqualTo(verificationPath)).withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

    assertThatExceptionOfType(FeignException.class).isThrownBy(() -> underTest.verifyTan(randomUUID));
  }

  @Test
  void checkTimeout() {
    server.stubFor(
        post(urlEqualTo(verificationPath))
            .withRequestBody(matchingJsonPath("tan", equalTo(randomUUID)))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(ok().withFixedDelay(1000)));

    assertThatExceptionOfType(FeignException.class).isThrownBy(() -> underTest.verifyTan(randomUUID));
  }


}
