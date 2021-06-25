

package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.Rule;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import java.util.List;
import java.util.Optional;

/**
 * This is a Spring Cloud Feign based HTTP client that allows type-safe HTTP calls and abstract the implementation
 * away.
 */
public interface DigitalCovidCertificateClient {

  List<String> getCountryList();

  List<ValueSetMetadata> getValueSets();

  Optional<ValueSet> getValueSet(String hash);

  List<Rule> getRules();

  Optional<Rule> getCountryRuleByHash(String country, String hash);

  List<Rule> getCountryRules(String country);
}
