package app.coronawarn.server.services.submission.verification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "verification-server", url = "${services.submission.verification.base-url}")
public interface VerificationServerClient {
  @PostMapping(value = "${services.submission.verification.path}", consumes = "application/json")
  String verfiyTan(Tan tan);
}
