
package app.coronawarn.server.services.distribution.dgc.client;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;

import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;


/**
 * This is an implementation with test data for interface retrieving Digital Covid Certificate data. Used to retrieve
 * mock sample data from classpath.
 */
@Component
@Profile("fake-dcc-client")
public class TestDigitalCovidCertificateClient implements DigitalCovidCertificateClient {

  public static final String TEST_TYPE_HASH = "50ba87d7c774cd9d77e4d82f6ab34871119bc4ad51b5b6fa1100efa687be0094";
  public static final String AGENT_TARGETED_HASH = "d4bfba1fd9f2eb29dfb2938220468ccb0b481d348f192e6015d36da4b911a83a";

  public static final String RULE_1_HASH = "7221d518570fe9f4417c482ff0d2582a7b6440f243a9034f812e0d71611b611f";
  public static final String RULE_2_HASH = "6821d518570fe9f4417c482ff0d2582a7b6440f243a9034f812e0d71611b611f";
  public static final String RULE_3_HASH = "7021d518570fe9f4417c482ff0d2582a7b6440f243a9034f812e0d71611b611f";

  private final ResourceLoader resourceLoader;

  public TestDigitalCovidCertificateClient(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public List<String> getCountryList() throws DigitalCovidCertificateException {
    try {
      return Arrays.asList(readConfiguredJsonOrDefault(resourceLoader, null,
          "dgc/country-list.json", String[].class));
    } catch (UnableToLoadFileException e) {
      throw new DigitalCovidCertificateException("Problem occurred while retrieving default country list: ", e);
    }
  }

  @Override
  public List<ValueSetMetadata> getValueSets() throws DigitalCovidCertificateException {
    try {
      return Arrays.asList(readConfiguredJsonOrDefault(resourceLoader, null,
          "dgc/valuesets.json", ValueSetMetadata[].class));
    } catch (UnableToLoadFileException e) {
      throw new DigitalCovidCertificateException("Problem occurred while retrieving valuesets", e);
    }
  }

  @Override
  public Optional<ValueSet> getValueSet(String hash) throws DigitalCovidCertificateException {
    try {
      switch (hash) {
        case TEST_TYPE_HASH:
          return Optional.ofNullable(readConfiguredJsonOrDefault(resourceLoader, null,
              "dgc/test-type.json", ValueSet.class));
        case AGENT_TARGETED_HASH:
          return Optional.ofNullable(readConfiguredJsonOrDefault(resourceLoader, null,
              "dgc/disease-agent-targeted.json", ValueSet.class));
        default:
          return Optional.ofNullable(readConfiguredJsonOrDefault(resourceLoader, null,
              "dgc/file-does-not-exist.json", ValueSet.class));
      }
    } catch (UnableToLoadFileException e) {
      throw new DigitalCovidCertificateException("Problem occurred while retrieving valueset with hash: " + hash, e);
    }
  }

  @Override
  public List<BusinessRuleItem> getRules() throws DigitalCovidCertificateException {
    try {
      return Arrays.asList(readConfiguredJsonOrDefault(resourceLoader, null,
          "dgc/rules.json", BusinessRuleItem[].class));
    } catch (UnableToLoadFileException e) {
      throw new DigitalCovidCertificateException("Problem occurred while retrieving default rules list: ", e);
    }
  }

  @Override
  public Optional<BusinessRule> getCountryRuleByHash(String country, String hash)
      throws DigitalCovidCertificateException {
    try {
      switch (hash) {
        case RULE_1_HASH:
          return Optional.ofNullable(readConfiguredJsonOrDefault(resourceLoader, null,
              "dgc/rule_1.json", BusinessRule.class));
        case RULE_2_HASH:
          return Optional.ofNullable(readConfiguredJsonOrDefault(resourceLoader, null,
              "dgc/rule_2.json", BusinessRule.class));
        case RULE_3_HASH:
          return Optional.ofNullable(readConfiguredJsonOrDefault(resourceLoader, null,
              "dgc/rule_3.json", BusinessRule.class));
        default:
          throw new DigitalCovidCertificateException("No rule found for country: " + country + " and hash: " + hash);
      }
    } catch (UnableToLoadFileException e) {
      throw new DigitalCovidCertificateException("Problem finding rules JSON: ", e);
    }
  }

}
