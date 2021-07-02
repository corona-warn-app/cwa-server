package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;

import app.coronawarn.server.common.protocols.internal.dgc.ValueSetItem;
import app.coronawarn.server.common.protocols.internal.dgc.ValueSets;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;


@Component
public class DigitalGreenCertificateToProtobufMapping {

  static final String TEST_RESULT_ID = "covid-19-lab-result";
  static final String TEST_MANF_ID = "covid-19-lab-test-manufacturer-and-name";
  static final String TEST_TYPE_ID = "covid-19-lab-test-type";
  static final String DISEASE_AGENT_TARGETED_ID = "disease-agent-targeted";
  static final String VACCINE_PROPHYLAXIS_ID = "sct-vaccines-covid-19";
  static final String VACCINE_MAH_ID = "vaccines-covid-19-auth-holders";
  static final String VACCINE_MEDICINAL_PRODUCT_ID = "vaccines-covid-19-names";

  public static final String DISEASE_AGENT_TARGETED_DEFAULT_PATH = "dgc/disease-agent-targeted.json";
  public static final String VACCINE_MAH_DEFAULT_PATH = "dgc/vaccine-mah.json";
  public static final String VACCINE_MEDICINAL_PRODUCT_DEFAULT_PATH = "dgc/vaccine-medicinal-product.json";
  public static final String VACCINE_PROPHYLAXIS_DEFAULT_PATH = "dgc/vaccine-prophylaxis.json";
  public static final String TEST_MANF_DEFAULT_PATH = "dgc/test-manf.json";
  public static final String TEST_RESULT_DEFAULT_PATH = "dgc/test-result.json";
  public static final String TEST_TYPE_DEFAULT_PATH = "dgc/test-type.json";

  private static final Logger logger = LoggerFactory.getLogger(DigitalGreenCertificateToProtobufMapping.class);

  @Autowired
  DigitalCovidCertificateClient dccClient;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  ResourceLoader resourceLoader;

  private List<ValueSetMetadata> metadata;

  /**
   * Read the JSON for the marketing authorization holders.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readMahJson() throws UnableToLoadFileException {
    Optional<ValueSet> result = Optional.ofNullable(getValueSet(VACCINE_MAH_ID).or(() -> {
      String path = distributionServiceConfig.getDigitalGreenCertificate().getMahJsonPath();
      return readConfiguredJsonOrDefault(resourceLoader, path, VACCINE_MAH_DEFAULT_PATH,
          ValueSet.class);
    }).orElseThrow(() -> new UnableToLoadFileException(VACCINE_MAH_DEFAULT_PATH)));
    return result.get();
  }

  /**
   * Read the JSON for the medicinal products.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readMedicinalProductJson() throws UnableToLoadFileException {
    Optional<ValueSet> result = Optional.ofNullable(getValueSet(VACCINE_MEDICINAL_PRODUCT_ID).or(() -> {
      String path = distributionServiceConfig.getDigitalGreenCertificate().getMedicinalProductsJsonPath();
      return readConfiguredJsonOrDefault(resourceLoader, path, VACCINE_MEDICINAL_PRODUCT_DEFAULT_PATH,
          ValueSet.class);
    }).orElseThrow(() -> new UnableToLoadFileException(VACCINE_MEDICINAL_PRODUCT_DEFAULT_PATH)));
    return result.get();
  }

  /**
   * Read the JSON for the prophylaxis.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readProphylaxisJson() throws UnableToLoadFileException {
    Optional<ValueSet> result = Optional.ofNullable(getValueSet(VACCINE_PROPHYLAXIS_ID).or(() -> {
      String path = distributionServiceConfig.getDigitalGreenCertificate().getProphylaxisJsonPath();
      return readConfiguredJsonOrDefault(resourceLoader, path, VACCINE_PROPHYLAXIS_DEFAULT_PATH,
          ValueSet.class);
    }).orElseThrow(() -> new UnableToLoadFileException(VACCINE_PROPHYLAXIS_DEFAULT_PATH)));
    return result.get();
  }

  /**
   * Read the JSON for the disease or agent targeted.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readDiseaseAgentTargetedJson() throws UnableToLoadFileException {
    Optional<ValueSet> result = Optional.ofNullable(getValueSet(DISEASE_AGENT_TARGETED_ID).or(() -> {
      String path = distributionServiceConfig.getDigitalGreenCertificate().getDiseaseAgentTargetedJsonPath();
      return readConfiguredJsonOrDefault(resourceLoader, path, DISEASE_AGENT_TARGETED_DEFAULT_PATH,
          ValueSet.class);
    }).orElseThrow(() -> new UnableToLoadFileException(DISEASE_AGENT_TARGETED_DEFAULT_PATH)));
    return result.get();
  }

  /**
   * Read the JSON for the Rapid Antigen Test name and manufacturer.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readTestManfJson() throws UnableToLoadFileException {
    Optional<ValueSet> result = Optional.ofNullable(getValueSet(TEST_MANF_ID).or(() -> {
      String path = distributionServiceConfig.getDigitalGreenCertificate().getTestManfJsonPath();
      return readConfiguredJsonOrDefault(resourceLoader, path, TEST_MANF_DEFAULT_PATH,
          ValueSet.class);
    }).orElseThrow(() -> new UnableToLoadFileException(TEST_MANF_DEFAULT_PATH)));
    return result.get();
  }

  /**
   * Read the JSON for the test Result.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readTestResultJson() throws UnableToLoadFileException {
    Optional<ValueSet> result = Optional.ofNullable(getValueSet(TEST_RESULT_ID).or(() -> {
      String path = distributionServiceConfig.getDigitalGreenCertificate().getTestResultJsonPath();
      return readConfiguredJsonOrDefault(resourceLoader, path, TEST_RESULT_DEFAULT_PATH,
          ValueSet.class);
    }).orElseThrow(() -> new UnableToLoadFileException(TEST_RESULT_DEFAULT_PATH)));
    return result.get();
  }

  /**
   * Read the JSON for the type of Test.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readTestTypeJson() throws UnableToLoadFileException {
    Optional<ValueSet> result = Optional.ofNullable(getValueSet(TEST_TYPE_ID).or(() -> {
      String path = distributionServiceConfig.getDigitalGreenCertificate().getTestTypeJsonPath();
      return readConfiguredJsonOrDefault(resourceLoader, path, TEST_TYPE_DEFAULT_PATH,
          ValueSet.class);
    }).orElseThrow(() -> new UnableToLoadFileException(TEST_TYPE_DEFAULT_PATH)));
    return result.get();
  }

  /**
   * Create the Protobuf from JSON.
   *
   * @return the protobuf filled with values from JSON.
   */
  public ValueSets constructProtobufMapping() throws UnableToLoadFileException {
    List<ValueSetItem> mahItems = toValueSetItems(readMahJson().getValueSetValues());
    List<ValueSetItem> productItems = toValueSetItems(readMedicinalProductJson().getValueSetValues());
    List<ValueSetItem> prophylaxisItems = toValueSetItems(readProphylaxisJson().getValueSetValues());
    List<ValueSetItem> diseaseAgentTargetedItems = toValueSetItems(readDiseaseAgentTargetedJson().getValueSetValues());
    List<ValueSetItem> testManfItems = toValueSetItems(readTestManfJson().getValueSetValues());
    List<ValueSetItem> testResultItems = toValueSetItems(readTestResultJson().getValueSetValues());
    List<ValueSetItem> testTypeItems = toValueSetItems(readTestTypeJson().getValueSetValues());

    return ValueSets.newBuilder()
        .setMa(app.coronawarn.server.common.protocols.internal.dgc.ValueSet.newBuilder().addAllItems(mahItems).build())
        .setMp(
            app.coronawarn.server.common.protocols.internal.dgc.ValueSet.newBuilder().addAllItems(productItems).build())
        .setVp(app.coronawarn.server.common.protocols.internal.dgc.ValueSet.newBuilder().addAllItems(prophylaxisItems)
            .build())
        .setTg(app.coronawarn.server.common.protocols.internal.dgc.ValueSet.newBuilder()
            .addAllItems(diseaseAgentTargetedItems).build())
        .setTcMa(app.coronawarn.server.common.protocols.internal.dgc.ValueSet.newBuilder().addAllItems(testManfItems)
            .build())
        .setTcTr(app.coronawarn.server.common.protocols.internal.dgc.ValueSet.newBuilder().addAllItems(testResultItems)
            .build())
        .setTcTt(app.coronawarn.server.common.protocols.internal.dgc.ValueSet.newBuilder().addAllItems(testTypeItems)
            .build())
        .build();
  }

  private List<ValueSetItem> toValueSetItems(Map<String, ValueSetObject> valueSetValues) {
    return valueSetValues.entrySet().stream().map(
        entry -> (ValueSetItem.newBuilder()
            .setKey(entry.getKey())
            .setDisplayText(entry.getValue().getDisplay())).build())
        .collect(Collectors.toList());
  }

  private Optional<ValueSet> getValueSet(String valueSetId) {
    Optional<String> hash = getValueSetHash(valueSetId);
    if (hash.isPresent()) {
      return dccClient.getValueSet(hash.get());
    }
    return Optional.empty();
  }

  private Optional<String> getValueSetHash(String valueSetId) {
    if (metadata == null) {
      metadata = dccClient.getValueSets();
      if (metadata == null) {
        logger.error("Tried to retrieve ValueSets metadata from {}, but received null!", dccClient);
        return Optional.empty();
      }
    }
    for (ValueSetMetadata metadataItem : metadata) {
      if (metadataItem.getId().equals(valueSetId)) {
        return Optional.of(metadataItem.getHash());
      }
    }
    logger.error("Didn't find the ValueSets for valueSetId {}.", valueSetId);
    return Optional.empty();
  }
}

