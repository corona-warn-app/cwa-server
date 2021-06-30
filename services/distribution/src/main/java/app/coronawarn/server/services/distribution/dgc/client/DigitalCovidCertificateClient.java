

package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import java.util.List;
import java.util.Optional;

/**
 * This is a Spring Cloud Feign based HTTP client that allows type-safe HTTP calls and abstract the implementation
 * away.
 */
public interface DigitalCovidCertificateClient {

  List<String> getCountryList() throws DigitalCovidCertificateException;

  List<ValueSetMetadata> getValueSets();

  Optional<ValueSet> getValueSet(String hash);

  List<BusinessRuleItem> getRules() throws DigitalCovidCertificateException;

  Optional<BusinessRule> getCountryRuleByHash(String country, String hash) throws DigitalCovidCertificateException;

  List<BusinessRule> getCountryRules(String country);
}
