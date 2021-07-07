

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
 * Used to make HTTP request to Digital Covid Certificate server.
 */
@FeignClient(name = "dcc-server", configuration = CloudDccFeignClientConfiguration.class,
    url = "${services.distribution.digital-green-certificate.client.base-url}")
public interface DigitalCovidCertificateFeignClient {

  /**
   * HTTP GET to return all onboarded countries.
   */
  @Timed
  @GetMapping(value = "${services.distribution.digital-green-certificate.client.country-list-path}")
  ResponseEntity<List<String>> getCountryList();

  /**
   * HTTP GET to return a specific valuesets based on its hash.
   *
   * @param hash - valueset hash
   */
  @Timed
  @GetMapping(value = "${services.distribution.digital-green-certificate.client.value-sets-path}/{hash}")
  ResponseEntity<ValueSet> getValueSet(@PathVariable String hash);

  /**
   * HTTP GET to return all valuesets.
   */
  @Timed
  @GetMapping(value = "${services.distribution.digital-green-certificate.client.value-sets-path}")
  ResponseEntity<List<ValueSetMetadata>> getValueSets();

  /**
   * HTTP GET to return all business rules.
   */
  @Timed
  @GetMapping(value = "${services.distribution.digital-green-certificate.client.rules-path}")
  ResponseEntity<List<BusinessRuleItem>> getRules();

  /**
   * HTTP GET to return a specific business rule based on its country and hash.
   *
   * @param country - business rule country code.
   * @param hash - business rule hash
   */
  @Timed
  @GetMapping(value = "${services.distribution.digital-green-certificate.client.rules-path}/{country}/{hash}")
  ResponseEntity<BusinessRule> getCountryRule(@PathVariable String country, @PathVariable String hash);
}
