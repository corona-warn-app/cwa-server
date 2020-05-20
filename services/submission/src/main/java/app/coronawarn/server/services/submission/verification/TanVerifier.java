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

import app.coronawarn.server.services.submission.verification.tan.TanAuthorization;
import app.coronawarn.server.services.submission.verification.tan.TanAuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The TanVerifier performs the verification of submission TANs.
 */
@Component("tanVerifier")
public class TanVerifier {

  private static final Logger logger = LoggerFactory.getLogger(TanVerifier.class);

  /**
   * Verifies the specified TAN. Returns {@literal true} if the specified TAN is valid, {@literal false} otherwise.
   *
   * @param tan Submission Authorization TAN, in format "(TAN|TELETAN) KEY"
   * @return {@literal true} if the specified TAN is valid, {@literal false} otherwise.
   */
  public boolean verifyTan(String tan) {
    try {
      var tanAuthorization = TanAuthorization.of(tan);

      return validateAuthorization(tanAuthorization);
    } catch (TanAuthorizationException e) {
      logger.info("Failed TAN validation", e);
      return false;
    }
  }

  private boolean validateAuthorization(TanAuthorization tanAuthorization) {
    var authType = tanAuthorization.getAuthType();

    switch (authType) {
      case TAN:
        return validateTan(tanAuthorization.getKey());
      case TELETAN:
        return validateTeleTan(tanAuthorization.getKey());
      default:
        throw new UnsupportedOperationException("Authorization Type not supported: " + authType);
    }
  }

  private boolean validateTan(String tan) {
    // FIXME Add implementation
    return true;
  }

  private boolean validateTeleTan(String teleTan) {
    // FIXME Add implementation
    return true;
  }
}
