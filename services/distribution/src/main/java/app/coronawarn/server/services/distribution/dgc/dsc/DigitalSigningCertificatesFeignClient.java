package app.coronawarn.server.services.distribution.dgc.dsc;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This is a Spring Cloud Feign based HTTP client that allows type-safe HTTP calls and abstract the implementation away.
 * Used to make HTTP request to Digital Signign Certificates server.
 */
@FeignClient(name = "dsc-server", configuration = CloudDscFeignClientConfiguration.class,
    url = "${services.distribution.digital-green-certificate.dsc-client.base-url}")
public interface DigitalSigningCertificatesFeignClient {

  /**
   * HTTP GET to return all DSC trust objects.
   */
  @Timed
  @GetMapping(value = "${services.distribution.digital-green-certificate.dsc-client.dsc-list-path}")
  ResponseEntity<String> getDscTrustList();
}
