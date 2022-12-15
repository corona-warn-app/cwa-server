package app.coronawarn.server.services.submission.monitoring;

import app.coronawarn.server.services.submission.verification.Tan;
import app.coronawarn.server.services.submission.verification.VerificationServerClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator exposed in the readiness probe of the application.
 * Fires NULL UUID tan to the verification service, and checks that the
 * response code is 2xx or 404, else sets health to down, and marks
 * application as not ready for requests.
 */
@Component("verificationService")
public class VerificationServiceHealthIndicator implements HealthIndicator {

  private static final Logger logger = LoggerFactory.getLogger(VerificationServiceHealthIndicator.class);

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
      logger.info(e.getLocalizedMessage());
    } catch (Exception e) {
      // http status code is neither 2xx nor 404
      logger.error(e.getLocalizedMessage(), e);
      return Health.down(e).build();
    }
    return Health.up().build();
  }
}
