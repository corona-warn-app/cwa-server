
package app.coronawarn.server.services.distribution.dgc.client;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;

import app.coronawarn.server.common.shared.exception.DefaultValueSetsMissingException;
import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;


@Component
@Profile("fake-dcc-client")
public class TestDigitalCovidCertificateClient implements DigitalCovidCertificateClient {

  public static final String TEST_TYPE_HASH = "50ba87d7c774cd9d77e4d82f6ab34871119bc4ad51b5b6fa1100efa687be0094";
  public static final String AGENT_TARGETED_HASH = "d4bfba1fd9f2eb29dfb2938220468ccb0b481d348f192e6015d36da4b911a83a";

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
  public List<ValueSetMetadata> getValueSets() {
    try {
      return Arrays.asList(readConfiguredJsonOrDefault(resourceLoader, null,
          "dgc/valuesets.json", ValueSetMetadata[].class));
    } catch (DefaultValueSetsMissingException e) {
      e.printStackTrace();
    }

    return Collections.emptyList();
  }

  @Override
  public Optional<ValueSet> getValueSet(String hash) {
    try {
      switch (hash) {
        case TEST_TYPE_HASH:
          return Optional.ofNullable(readConfiguredJsonOrDefault(resourceLoader, null,
              "dgc/test-type.json", ValueSet.class));
        case AGENT_TARGETED_HASH:
          return Optional.ofNullable(readConfiguredJsonOrDefault(resourceLoader, null,
              "dgc/agent-targeted.json", ValueSet.class));
        default:
          return Optional.ofNullable(readConfiguredJsonOrDefault(resourceLoader, null,
              "dgc/vaccine-mah.json", ValueSet.class));
      }

    } catch (DefaultValueSetsMissingException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  @Override
  public List<BusinessRuleItem> getRules() {
    try {
      return Arrays.asList(readConfiguredJsonOrDefault(resourceLoader, null,
          "dgc/rules.json", BusinessRuleItem[].class));
    } catch (DefaultValueSetsMissingException e) {
      e.printStackTrace();
    }

    return Collections.emptyList();
  }

  @Override
  public Optional<BusinessRule> getCountryRuleByHash(String country, String hash) {
    try {
      return Optional.ofNullable(readConfiguredJsonOrDefault(resourceLoader, null,
          "dgc/rule.json", BusinessRule.class));
    } catch (DefaultValueSetsMissingException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  @Override
  public List<BusinessRule> getCountryRules(String country) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

}
