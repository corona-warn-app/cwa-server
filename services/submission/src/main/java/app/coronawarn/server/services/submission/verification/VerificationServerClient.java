

package app.coronawarn.server.services.submission.verification;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * This is a Spring Cloud Feign based HTTP client that allows type-safe HTTP calls and abstract the implementation
 * away.
 */
@FeignClient(name = "verification-server", configuration = VerificationServerClientConfiguration.class,
    url = "${services.submission.verification.base-url}")
public interface VerificationServerClient {

  /**
   * This methods calls the verification service with the given {#link tan}.
   *
   * @param tan the tan to verify.
   * @return 404 when the tan is not valid.
   */
  @Timed
  @PostMapping(value = "${services.submission.verification.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<Void> verifyTan(Tan tan);
}
