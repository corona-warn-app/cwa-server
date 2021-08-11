package app.coronawarn.server.services.distribution.dgc;

import app.coronawarn.server.common.protocols.internal.dgc.ValueSetItem;
import app.coronawarn.server.common.protocols.internal.dgc.ValueSets;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
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
  ValueSet readMahJson() throws FetchValueSetsException {
    return getValueSet(VACCINE_MAH_ID);
  }

  /**
   * Read the JSON for the medicinal products.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readMedicinalProductJson() throws FetchValueSetsException {
    return getValueSet(VACCINE_MEDICINAL_PRODUCT_ID);
  }

  /**
   * Read the JSON for the prophylaxis.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readProphylaxisJson() throws FetchValueSetsException {
    return getValueSet(VACCINE_PROPHYLAXIS_ID);
  }

  /**
   * Read the JSON for the disease or agent targeted.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readDiseaseAgentTargetedJson() throws FetchValueSetsException {
    return getValueSet(DISEASE_AGENT_TARGETED_ID);
  }

  /**
   * Read the JSON for the Rapid Antigen Test name and manufacturer.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readTestManfJson() throws FetchValueSetsException {
    return getValueSet(TEST_MANF_ID);
  }

  /**
   * Read the JSON for the test Result.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readTestResultJson() throws FetchValueSetsException {
    return getValueSet(TEST_RESULT_ID);
  }

  /**
   * Read the JSON for the type of Test.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readTestTypeJson() throws FetchValueSetsException {
    return getValueSet(TEST_TYPE_ID);
  }

  /**
   * Create the Protobuf from JSON.
   *
   * @return the protobuf filled with values from JSON.
   */
  public ValueSets constructProtobufMapping() throws FetchValueSetsException {
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

  /**
   * Read the valueSetHash for the given ID from the metadata, and then the valueSet for the returned hash.
   *
   * @param valueSetId The ID of the valueSet to resolve.
   * @return The ValueSet or empty.
   */
  private ValueSet getValueSet(String valueSetId) throws FetchValueSetsException {
    Optional<String> hash = getValueSetHash(valueSetId);

    if (hash.isPresent()) {
      return dccClient.getValueSet(hash.get());
    } else {
      throw new FetchValueSetsException("Hash not found for value set id: " + valueSetId);
    }
  }

  /**
   * Get the hash for the given ValueSetId from the metadata.
   *
   * @param valueSetId The valueSetId to get the hash for.
   * @return The hash as contained in the metadata or empty.
   */
  private Optional<String> getValueSetHash(String valueSetId) throws FetchValueSetsException {
    if (metadata == null) {
      // feign client either returns a non-null metadata or throw FetchValueSetsException.
      metadata = dccClient.getValueSets();
    }

    return metadata.stream()
        .filter(metadataItem -> metadataItem.getId().equals(valueSetId))
        .map(ValueSetMetadata::getHash)
        .findFirst();
  }
}
