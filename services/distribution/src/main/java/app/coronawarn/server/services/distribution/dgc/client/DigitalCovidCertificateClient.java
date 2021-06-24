

package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.Rule;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import java.util.List;
import java.util.Optional;

/**
 * This is a Spring Cloud Feign based HTTP client that allows type-safe HTTP calls and abstract the implementation
 * away.
 */
public interface DigitalCovidCertificateClient {

  /**
   * This methods calls the verification service with the given {#link tan}.
   *
   * @return 404 when the tan is not valid.
   */
  List<String> getCountryList();

  List<ValueSet> getValueSets();

  Optional<ValueSet> getValueSet(String hash);

  List<Rule> getRules();

  Optional<Rule> getCountryRule(String country, String hash);
}
