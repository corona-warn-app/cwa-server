
package app.coronawarn.server.services.distribution.dgc.client;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.DISEASE_AGENT_TARGETED_DEFAULT_PATH;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.TEST_MANF_DEFAULT_PATH;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.TEST_RESULT_DEFAULT_PATH;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.TEST_TYPE_DEFAULT_PATH;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.VACCINE_MAH_DEFAULT_PATH;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.VACCINE_MEDICINAL_PRODUCT_DEFAULT_PATH;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.VACCINE_PROPHYLAXIS_DEFAULT_PATH;

import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import java.util.Arrays;
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

  public static final String DISEASE_AGENT_TARGETED_HASH =
      "d4bfba1fd9f2eb29dfb2938220468ccb0b481d348f192e6015d36da4b911a83a";
  public static final String VACCINE_MAH_HASH = "55af9c705a95ced1a7d9130043f71a7a01f72e168dbd451d23d1575962518ab6";
  public static final String VACCINE_MEDICINAL_PRODUCT_HASH =
      "8651c3db9ed5332c8fa42943d4656d442a5264debc8482b6d11d4c9176149146";
  public static final String VACCINE_PROPHYLAXIS_HASH =
      "70505eab33ac1da351f782ee2e78e89451226c47360e7b89b8a6295bbb70eed6";
  public static final String TEST_MANF_HASH = "9da3ed15d036c20339647f8db1cb67bfcfbd04575e10b0c0df8e55a76a173a97";
  public static final String TEST_RESULT_HASH = "934e145e9bb1f560d1d3b1ec767ce3a4e9f86ae101260ed04a5cef8c1f5636c4";
  public static final String TEST_TYPE_HASH = "50ba87d7c774cd9d77e4d82f6ab34871119bc4ad51b5b6fa1100efa687be0094";

  public static final String RULE_1_HASH = "7221d518570fe9f4417c482ff0d2582a7b6440f243a9034f812e0d71611b611f";
  public static final String RULE_2_HASH = "6821d518570fe9f4417c482ff0d2582a7b6440f243a9034f812e0d71611b611f";
  public static final String RULE_3_HASH = "7021d518570fe9f4417c482ff0d2582a7b6440f243a9034f812e0d71611b611f";
  public static final String DGC_FILE_DOES_NOT_EXIST_JSON = "dgc/file-does-not-exist.json";

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
        case DISEASE_AGENT_TARGETED_HASH:
          return readDefault(DISEASE_AGENT_TARGETED_DEFAULT_PATH);
        case VACCINE_MAH_HASH:
          return readDefault(VACCINE_MAH_DEFAULT_PATH);
        case VACCINE_MEDICINAL_PRODUCT_HASH:
          return readDefault(VACCINE_MEDICINAL_PRODUCT_DEFAULT_PATH);
        case VACCINE_PROPHYLAXIS_HASH:
          return readDefault(VACCINE_PROPHYLAXIS_DEFAULT_PATH);
        case TEST_MANF_HASH:
          return readDefault(TEST_MANF_DEFAULT_PATH);
        case TEST_RESULT_HASH:
          return readDefault(TEST_RESULT_DEFAULT_PATH);
        case TEST_TYPE_HASH:
          return readDefault(TEST_TYPE_DEFAULT_PATH);
        default:
          return readDefault(DGC_FILE_DOES_NOT_EXIST_JSON);
      }
    } catch (UnableToLoadFileException e) {
      throw new DigitalCovidCertificateException("Problem occurred while retrieving valueset with hash: " + hash, e);
    }
  }

  private Optional<ValueSet> readDefault(String valueSetId) throws UnableToLoadFileException {
    return Optional.ofNullable(
        readConfiguredJsonOrDefault(resourceLoader, null, valueSetId, ValueSet.class));
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
