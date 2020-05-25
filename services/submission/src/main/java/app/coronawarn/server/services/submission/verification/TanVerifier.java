/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.submission.verification;

import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * The TanVerifier performs the verification of submission TANs.
 */
@Service
public class TanVerifier {

  private static final Logger logger = LoggerFactory.getLogger(TanVerifier.class);
  private final String verificationServiceUrl;
  private final RestTemplate restTemplate;
  private final HttpHeaders requestHeader = new HttpHeaders();

  /**
   * This class can be used to verify a TAN against a configured verification service.
   *
   * @param submissionServiceConfig A submission service configuration
   * @param restTemplateBuilder A rest template builder
   */
  @Autowired
  public TanVerifier(SubmissionServiceConfig submissionServiceConfig, RestTemplateBuilder restTemplateBuilder) {
    this.verificationServiceUrl = submissionServiceConfig.getVerificationBaseUrl()
      + submissionServiceConfig.getVerificationPath();
    this.restTemplate = restTemplateBuilder.build();
    this.requestHeader.setContentType(MediaType.APPLICATION_JSON);
  }

  /**
   * Verifies the specified TAN. Returns {@literal true} if the specified TAN is valid, {@literal false} otherwise.
   *
   * @param tan Submission Authorization TAN
   * @return {@literal true} if the specified TAN is valid, {@literal false} otherwise.
   * @throws RestClientException if status code is neither 2xx nor 4xx
   */
  public boolean verifyTan(String tan) {
    String trimmedTan = tan.trim();

    if (!checkTanSyntax(trimmedTan)) {
      logger.debug("TAN Syntax check failed for TAN: {}", trimmedTan);
      return false;
    }

    return verifyWithVerificationService(trimmedTan);
  }

  /**
   * Verifies if the provided TAN can be parsed as a UUID.
   *
   * @param tan Submission Authorization TAN
   * @return {@literal true} if tan can be parsed as a UUID, {@literal false} otherwise
   */
  private boolean checkTanSyntax(String tan) {
    try {
      UUID.fromString(tan);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Queries the configured verification service to validate the provided TAN.
   *
   * @param tan Submission Authorization TAN
   * @return {@literal true} if verification service is able to verify the provided TAN, {@literal false} otherwise
   * @throws RestClientException if http status code is neither 2xx nor 404
   */
  private boolean verifyWithVerificationService(String tan) {
    String json = "{ \"tan\": \"" + tan + "\" }";
    HttpEntity<String> entity = new HttpEntity<>(json, requestHeader);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(verificationServiceUrl, entity, String.class);
      return response.getStatusCode().is2xxSuccessful();
    } catch (HttpClientErrorException.NotFound e) {
      // The validation service returns http status 404 if the TAN is invalid
      logger.debug("TAN validation failed for TAN: {}", tan);
      return false;
    }
  }
}
