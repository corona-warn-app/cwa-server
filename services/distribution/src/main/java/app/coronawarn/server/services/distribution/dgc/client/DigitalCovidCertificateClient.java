

package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
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

  List<BusinessRuleItem> getRules();

  Optional<BusinessRule> getCountryRuleByHash(String country, String hash);

  List<BusinessRule> getCountryRules(String country);
}
