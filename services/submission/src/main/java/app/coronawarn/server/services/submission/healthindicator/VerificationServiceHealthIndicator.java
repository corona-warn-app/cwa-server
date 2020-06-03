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

package app.coronawarn.server.services.submission.healthindicator;

import app.coronawarn.server.services.submission.verification.Tan;
import app.coronawarn.server.services.submission.verification.VerificationServerClient;
import feign.FeignException;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator exposed in the readiness probe of the application.
 * Fires NULL UUID tan to the verification service, and checks that the
 * response code is 2xx or 404, else sets health to down, and marks
 * application as not ready for requests.
 */
@Component
public class VerificationServiceHealthIndicator implements HealthIndicator {

  private final VerificationServerClient verificationServerClient;

  VerificationServiceHealthIndicator(VerificationServerClient verificationServerClient) {
    this.verificationServerClient = verificationServerClient;
  }

  @Override
  public Health health() {
    try {
      verificationServerClient.verifyTan(Tan.of("00000000-0000-0000-0000-000000000000"));
    } catch (FeignException.NotFound e) {
      // expected
      return Health.up().build();
    } catch (Exception e) {
      // http status code is neither 2xx nor 404
      return Health.down().build();
    }
    return Health.up().build();
  }

}
