
package app.coronawarn.server.services.submission.verification;

import app.coronawarn.server.common.federation.client.hostname.NoopHostnameVerifierProvider;
import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.persistence.domain.config.TrlDerivations;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import feign.FeignException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@SpringBootTest(classes = { EventTanVerifier.class, CloudFeignClientProvider.class, TekFieldDerivations.class,
    TrlDerivations.class, NoopHostnameVerifierProvider.class })
@ImportAutoConfiguration({ FeignAutoConfiguration.class, FeignTestConfiguration.class })
@EnableConfigurationProperties(value = SubmissionServiceConfig.class)
@EnableFeignClients
@DirtiesContext
@ActiveProfiles({ "feign", "disable-ssl-client-verification-verify-hostname" })
class EventTanVerifierTest {

  @Autowired
  private EventTanVerifier eventTanVerifier;

  @Autowired
  private SubmissionServiceConfig submissionServiceConfig;

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
