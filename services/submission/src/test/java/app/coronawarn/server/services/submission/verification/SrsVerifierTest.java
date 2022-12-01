package app.coronawarn.server.services.submission.verification;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
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
class SrsVerifierTest {

  private static WireMockServer server;

  @BeforeAll
  static void setupWireMock() {
    server = new WireMockServer(options().port(1234)); // test/resources/application.yaml
    server.start();
  }

  @AfterAll
  static void tearDown() {
    server.stop();
  }

  static final String TOKEN = "otp";

  private String path;

  private String randomUuid;

  @Autowired
  private SubmissionServiceConfig submissionServiceConfig;

  @MockBean
  TestRestTemplate testRestTemplate;

  @Autowired
  private SrsOtpVerifier verifier;

  @BeforeEach
  void setup() {
    path = submissionServiceConfig.getSrsVerifyPath();
    assertEquals("/version/v1/srs", path);
    randomUuid = UUID.randomUUID().toString();
    server.resetAll();
  }

  @Test
  void checkInternalServerError() {
    server.stubFor(
        post(urlEqualTo(path)).withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

    assertThatExceptionOfType(FeignException.class).isThrownBy(() -> verifier.verifyTan(randomUuid));
  }

  @Test
  void checkInvalidOtp() {
    server.stubFor(
        post(urlEqualTo(path)).withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

    assertThat(verifier.verifyTan(randomUuid)).isFalse();
  }

  @Test
  void checkTimeout() {
    server.stubFor(
        post(urlEqualTo(path))
            .withRequestBody(matchingJsonPath(TOKEN, equalTo(randomUuid)))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withFixedDelay(1000)));

    assertThatExceptionOfType(FeignException.class).isThrownBy(() -> verifier.verifyTan(randomUuid));
  }

  @Test
  void checkTooLongOtp() {
    server.stubFor(
        post(urlEqualTo(path)).withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

    assertThat(verifier.verifyTan(randomUuid + randomUuid)).isFalse();
  }

  @Test
  void checkValidOtp() {
    server.stubFor(
        post(urlEqualTo(path))
            .withRequestBody(matchingJsonPath(TOKEN, equalTo(randomUuid)))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.OK.value())));

    assertThat(verifier.verifyTan(randomUuid)).isTrue();
  }
}
