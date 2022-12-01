package app.coronawarn.server.services.submission.verification;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * This is a Spring Cloud Feign based HTTP client that allows type-safe HTTP calls and abstract the implementation away.
 */
@FeignClient(name = "srs-verify", 
    configuration = VerificationServerClientConfiguration.class, 
              url = "${services.submission.srs-verify.base-url}")
public interface SrsVerifyClient {

  /**
   * This methods calls the srs-verify service with the given {@link Otp}.
   *
   * @param otp the {@link Otp} to verify.
   * @return 404 when the tan is not valid.
   */
  @Timed
  @PostMapping(value = "${services.submission.srs-verify.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<Void> verifyOtp(final Otp otp);
}
