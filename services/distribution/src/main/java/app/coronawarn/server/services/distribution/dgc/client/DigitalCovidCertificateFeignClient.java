

package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * This is a Spring Cloud Feign based HTTP client that allows type-safe HTTP calls and abstract the implementation
 * away.
 */
@FeignClient(name = "dcc-server", url = "${services.distribution.digital-green-certificate.client.base-url}")
public interface DigitalCovidCertificateFeignClient {

  /**
   * This methods calls the verification service with the given {#link tan}.
   *
   * @return 404 when the tan is not valid.
   */
  @Timed
  @GetMapping(value = "${services.distribution.digital-green-certificate.client.country-list-path}")
  ResponseEntity<List<String>> getCountryList();

  @Timed
  @GetMapping(value = "${services.distribution.digital-green-certificate.client.value-sets-path}/{hash}")
  ResponseEntity<ValueSet> getValueSet(@PathVariable String hash);

  @Timed
  @GetMapping(value = "${services.distribution.digital-green-certificate.client.value-sets-path}")
  ResponseEntity<List<ValueSetMetadata>> getValueSets();

  @Timed
  @GetMapping(value = "${services.distribution.digital-green-certificate.client.rules-path}")
  ResponseEntity<List<BusinessRuleItem>> getRules();

  @Timed
  @GetMapping(value = "${services.distribution.digital-green-certificate.client.rules-path}/{country}/{hash}")
  ResponseEntity<BusinessRule> getCountryRule(@PathVariable String country, @PathVariable String hash);
}
