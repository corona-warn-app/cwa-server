
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
    logger.info("Get country list");
    return digitalCovidCertificateClient.getCountryList().getBody();
  }

  @Override
  public List<ValueSetMetadata> getValueSets() {
    return digitalCovidCertificateClient.getValueSets().getBody();
  }

  @Override
  public Optional<ValueSet> getValueSet(String hash) {
    return Optional.ofNullable(digitalCovidCertificateClient.getValueSet(hash).getBody());
  }

  @Override
  public List<BusinessRuleItem> getRules() {
    return digitalCovidCertificateClient.getRules().getBody();
  }

  @Override
  public Optional<BusinessRule> getCountryRuleByHash(String country, String hash) {
    return Optional.ofNullable(digitalCovidCertificateClient.getCountryRule(country, hash).getBody());
  }

  @Override
  public List<BusinessRule> getCountryRules(String country) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

}
