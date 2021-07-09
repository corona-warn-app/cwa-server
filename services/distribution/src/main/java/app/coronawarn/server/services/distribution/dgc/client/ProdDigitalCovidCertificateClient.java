
package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This is an implementation with test data for interface retrieving Digital Covid Certificate data. Used to make HTTP
 * request to Digital Covid Certificate server.
 */
@Component
@Profile("!fake-dcc-client")
public class ProdDigitalCovidCertificateClient implements DigitalCovidCertificateClient {

  private static final Logger logger = LoggerFactory.getLogger(ProdDigitalCovidCertificateClient.class);

  private DigitalCovidCertificateFeignClient digitalCovidCertificateClient;

  public ProdDigitalCovidCertificateClient(DigitalCovidCertificateFeignClient digitalCovidCertificateFeignClient) {
    this.digitalCovidCertificateClient = digitalCovidCertificateFeignClient;
  }

  @Override
  public List<String> getCountryList() {
    logger.debug("Get country list from DCC");
    return digitalCovidCertificateClient.getCountryList().getBody();
  }

  @Override
  public List<ValueSetMetadata> getValueSets() {
    logger.debug("Get valuesets from DCC");
    return digitalCovidCertificateClient.getValueSets().getBody();
  }

  @Override
  public Optional<ValueSet> getValueSet(String hash) {
    logger.debug("Get valuesets having hash: " + hash + " from DCC");
    return Optional.ofNullable(digitalCovidCertificateClient.getValueSet(hash).getBody());
  }

  @Override
  public List<BusinessRuleItem> getRules() {
    logger.debug("Get rules from DCC");
    return digitalCovidCertificateClient.getRules().getBody();
  }

  @Override
  public Optional<BusinessRule> getCountryRuleByHash(String country, String hash) {
    logger.debug("Get business rule having country:" + country + " and hash: " + hash + "from DCC");
    return Optional.ofNullable(digitalCovidCertificateClient.getCountryRule(country, hash).getBody());
  }

}
