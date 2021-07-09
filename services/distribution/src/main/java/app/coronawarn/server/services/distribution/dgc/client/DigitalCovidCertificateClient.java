

package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import java.util.List;
import java.util.Optional;

/**
 * This is a wrapper interface retrieving Digital Covid Certificate data.
 * Used to make HTTP request to Digital Covid Certificate server.
 * Used to retrieve mock sample data from classpath.
 */
public interface DigitalCovidCertificateClient {

  List<String> getCountryList();

  List<ValueSetMetadata> getValueSets();

  Optional<ValueSet> getValueSet(String hash);

  List<BusinessRuleItem> getRules();

  Optional<BusinessRule> getCountryRuleByHash(String country, String hash);
}
