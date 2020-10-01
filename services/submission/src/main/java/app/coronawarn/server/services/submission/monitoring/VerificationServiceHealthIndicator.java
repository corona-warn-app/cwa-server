

package app.coronawarn.server.services.submission.monitoring;

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
