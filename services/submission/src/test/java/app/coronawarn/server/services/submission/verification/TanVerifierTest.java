/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

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
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {TanVerifier.class, DevelopmentFeignClientProvider.class, NoopHostnameVerifierProvider.class})
@ImportAutoConfiguration({FeignAutoConfiguration.class, FeignTestConfiguration.class})
@EnableConfigurationProperties(value = SubmissionServiceConfig.class)
@EnableFeignClients
@ActiveProfiles({ "feign", "disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname" })
class TanVerifierTest {

  @Autowired
  private TanVerifier tanVerifier;

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
  void checkValidTan() {
    server.stubFor(
        post(urlEqualTo(verificationPath))
            .withRequestBody(matchingJsonPath("tan", equalTo(randomUUID)))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.OK.value())));

    boolean tanVerificationResponse = tanVerifier.verifyTan(randomUUID);

    assertThat(tanVerificationResponse).isTrue();
  }

  @Test
  void checkInvalidTan() {
    server.stubFor(
        post(urlEqualTo(verificationPath))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

    boolean tanVerificationResponse = tanVerifier.verifyTan(randomUUID);

    assertThat(tanVerificationResponse).isFalse();
  }

  @Test
  void checkTooLongTan() {
    server.stubFor(
        post(urlEqualTo(verificationPath))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

    boolean tanVerificationResponse = tanVerifier.verifyTan(randomUUID + randomUUID);

    assertThat(tanVerificationResponse).isFalse();
  }

  @Test
  void checkInternalServerError() {
    server.stubFor(
        post(urlEqualTo(verificationPath))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

    assertThatExceptionOfType(FeignException.class).isThrownBy(() -> tanVerifier.verifyTan(randomUUID));
  }

}
