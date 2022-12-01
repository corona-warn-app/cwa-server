package app.coronawarn.server.services.submission.monitoring;

import app.coronawarn.server.services.submission.verification.Otp;
import app.coronawarn.server.services.submission.verification.SrsVerifyClient;
import app.coronawarn.server.services.submission.verification.Tan;
import feign.FeignException;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator exposed in the readiness probe of the application. Fires NULL UUID tan to the verification service,
 * and checks that the response code is 2xx or 404, else sets health to down, and marks application as not ready for
 * requests.
 */
@Component("srsVerifyService")
public class SrsVerifyServiceHealthIndicator implements HealthIndicator {

  private final SrsVerifyClient verificationServerClient;

  SrsVerifyServiceHealthIndicator(final SrsVerifyClient verificationServerClient) {
    this.verificationServerClient = verificationServerClient;
  }

  @Override
  public Health health() {
    try {
      verificationServerClient.verifyOtp(Otp.of(Tan.of("00000000-0000-0000-0000-000000000000")));
    } catch (final FeignException.NotFound e) {
      // expected
    } catch (final Exception e) {
      // http status code is neither 2xx nor 404
      return Health.down().build();
    }
    return Health.up().build();
  }
}
