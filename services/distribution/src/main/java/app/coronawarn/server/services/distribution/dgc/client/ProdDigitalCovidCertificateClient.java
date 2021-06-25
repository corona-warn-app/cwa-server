
package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.Rule;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@Profile("!fake-dcc-client")
public class ProdDigitalCovidCertificateClient implements DigitalCovidCertificateClient {

  private DigitalCovidCertificateFeignClient digitalCovidCertificateClient;

  public ProdDigitalCovidCertificateClient(DigitalCovidCertificateFeignClient digitalCovidCertificateFeignClient) {
    this.digitalCovidCertificateClient = digitalCovidCertificateFeignClient;
  }

  @Override
  public List<String> getCountryList() {
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
  public List<Rule> getRules() {
    return digitalCovidCertificateClient.getRules().getBody();
  }

  @Override
  public Optional<Rule> getCountryRuleByHash(String country, String hash) {
    return Optional.ofNullable(digitalCovidCertificateClient.getCountryRule(country, hash).getBody());
  }

  @Override
  public List<Rule> getCountryRules(String country) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

}
