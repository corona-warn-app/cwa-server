
package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.common.shared.exception.DefaultValueSetsMissingException;
import app.coronawarn.server.services.distribution.dgc.Rule;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;

@Component
@Profile("fake-dcc-client")
public class TestDigitalCovidCertificateClient implements DigitalCovidCertificateClient {

  @Autowired
  ResourceLoader resourceLoader;

  @Override
  public List<String> getCountryList() {
    try {
      return Arrays.asList(readConfiguredJsonOrDefault(resourceLoader, null, "dgc/country-list.json", String[].class));
    } catch (DefaultValueSetsMissingException e) {
      e.printStackTrace();
    }

    return Collections.emptyList();
  }

  @Override
  public List<ValueSet> getValueSets() {
    return Collections.emptyList();
  }

  @Override
  public Optional<ValueSet> getValueSet(String hash) {
    try {
      return Optional.ofNullable(readConfiguredJsonOrDefault(resourceLoader, null, "dgc/vaccine-mah.json", ValueSet.class));
    } catch (DefaultValueSetsMissingException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  @Override
  public List<Rule> getRules() {
    return Collections.emptyList();
  }

  @Override
  public Optional<Rule> getCountryRule(String country, String hash) {
    try {
      return Optional.ofNullable(readConfiguredJsonOrDefault(resourceLoader, null, "dgc/rule.json", Rule.class));
    } catch (DefaultValueSetsMissingException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

}
