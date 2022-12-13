package app.coronawarn.server.services.submission.verification;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
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
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import feign.FeignException;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
class SrsVerifierTest {

  private static WireMockServer srsVerifyMockServer;

  static final String TOKEN = "otp";

  /**
   * JSON string representation of new {@link SrsOtpRedemptionResponse}.
   *
   * @param otp   used in {@link SrsOtpRedemptionResponse}
   * @param state used in {@link SrsOtpRedemptionResponse}
   * @return JSON string
   */
  public static String response(final String otp, final OtpState state) {
    return new SrsOtpRedemptionResponse(otp, state, false).toString();
  }

  @BeforeAll
  static void setupWireMock() {
    srsVerifyMockServer = new WireMockServer(options().port(1234)); // fixed port as in test/resources/application.yaml
    srsVerifyMockServer.start();
  }

  @AfterAll
  static void tearDown() {
    srsVerifyMockServer.stop();
  }

  private String path;

  private String randomUuid;

  @Autowired
  private SubmissionServiceConfig submissionServiceConfig;

  @MockBean
  TestRestTemplate testRestTemplate;

  @Autowired
  private SrsOtpVerifier verifier;

  @Test
  void checkInternalServerError() {
    srsVerifyMockServer.stubFor(post(urlEqualTo(path)).withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON.toString()))
        .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

    assertThatExceptionOfType(FeignException.class).isThrownBy(() -> verifier.verifyTan(randomUuid));
  }

  @Test
  void checkInvalidOtp() {
    srsVerifyMockServer.stubFor(post(urlEqualTo(path)).withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON.toString()))
        .willReturn(aResponse().withStatus(NOT_FOUND.value())));

    assertThat(verifier.verifyTan(randomUuid)).isFalse();
  }

  @Test
  void checkTimeout() {
    srsVerifyMockServer.stubFor(
        post(urlEqualTo(path))
            .withRequestBody(matchingJsonPath(TOKEN, equalTo(randomUuid)))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(OK.value()).withFixedDelay(1000)));

    assertThatExceptionOfType(FeignException.class).isThrownBy(() -> verifier.verifyTan(randomUuid));
  }

  @Test
  void checkTooLongOtp() {
    srsVerifyMockServer.stubFor(post(urlEqualTo(path)).withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON.toString()))
        .willReturn(aResponse().withStatus(NOT_FOUND.value())));

    assertThat(verifier.verifyTan(randomUuid + randomUuid)).isFalse();
  }

  @ParameterizedTest
  @EnumSource(mode = Mode.EXCLUDE, names = "VALID")
  void checkUsedOtp(final OtpState state) {
    srsVerifyMockServer.stubFor(
        post(urlEqualTo(path))
            .withRequestBody(matchingJsonPath(TOKEN, equalTo(randomUuid)))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON.toString()))
            .willReturn(okForJson(response(randomUuid, state))));
    assertThat(verifier.verifyTan(randomUuid)).isFalse();
  }

  @Test
  void checkValidOtp() {
    srsVerifyMockServer.stubFor(
        post(urlEqualTo(path))
            .withRequestBody(matchingJsonPath(TOKEN, equalTo(randomUuid)))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON.toString()))
            .willReturn(okForJson(response(randomUuid, OtpState.VALID))));
    assertThat(verifier.verifyTan(randomUuid)).isTrue();
  }

  @BeforeEach
  void setup() {
    path = submissionServiceConfig.getSrsVerifyPath();
    assertEquals("/version/v1/srs", path);
    randomUuid = UUID.randomUUID().toString();
    srsVerifyMockServer.resetAll();
  }
}
