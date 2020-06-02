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

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * The TanVerifier performs the verification of submission TANs.
 */
@Service
public class TanVerifier {

  private static final Logger logger = LoggerFactory.getLogger(TanVerifier.class);
  private final VerificationServerClient verificationServerClient;

  /**
   * This class can be used to verify a TAN against a configured verification service.
   *
   * @param verificationServerClient The REST client to communicate with the verification server
   */
  public TanVerifier(VerificationServerClient verificationServerClient) {
    this.verificationServerClient = verificationServerClient;
  }

  /**
   * Verifies the specified TAN. Returns {@literal true} if the specified TAN is valid, {@literal false} otherwise.
   *
   * @param tanString Submission Authorization TAN
   * @return {@literal true} if the specified TAN is valid, {@literal false} otherwise.
   * @throws RestClientException if status code is neither 2xx nor 4xx
   */
  public boolean verifyTan(String tanString) {
    try {
      Tan tan = Tan.of(tanString);

      return verifyWithVerificationService(tan);
    } catch (IllegalArgumentException e) {
      logger.debug("TAN Syntax check failed for TAN: {}", tanString.trim());
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
  private boolean verifyWithVerificationService(Tan tan) {
    try {
      verificationServerClient.verifyTan(tan);
      return true;
    } catch (FeignException.NotFound e) {
      return false;
    }
  }
}
