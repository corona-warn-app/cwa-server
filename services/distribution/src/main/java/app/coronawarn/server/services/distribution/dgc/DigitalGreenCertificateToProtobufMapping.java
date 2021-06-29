package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;

import app.coronawarn.server.common.protocols.internal.dgc.ValueSetItem;
import app.coronawarn.server.common.protocols.internal.dgc.ValueSets;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;


@Component
public class DigitalGreenCertificateToProtobufMapping {

  private static final Logger logger = LoggerFactory.getLogger(DigitalGreenCertificateToProtobufMapping.class);

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  ResourceLoader resourceLoader;

  /**
   * Read the JSON for the marketing authorization holders.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readMahJson() throws UnableToLoadFileException {
    String path = distributionServiceConfig.getDigitalGreenCertificate().getMahJsonPath();
    return readConfiguredJsonOrDefault(resourceLoader, path, "dgc/vaccine-mah.json",
        ValueSet.class);
  }

  /**
   * Read the JSON for the medicinal products.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readMedicinalProductJson() throws UnableToLoadFileException {
    String path = distributionServiceConfig.getDigitalGreenCertificate().getMedicinalProductsJsonPath();
    return readConfiguredJsonOrDefault(resourceLoader, path, "dgc/vaccine-medicinal-product.json",
        ValueSet.class);
  }

  /**
   * Read the JSON for the prophylaxis.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readProphylaxisJson() throws UnableToLoadFileException {
    String path = distributionServiceConfig.getDigitalGreenCertificate().getProphylaxisJsonPath();
    return readConfiguredJsonOrDefault(resourceLoader, path, "dgc/vaccine-prophylaxis.json",
        ValueSet.class);
  }

  /**
   * Read the JSON for the disease or agent targeted.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readDiseaseAgentTargetedJson() throws UnableToLoadFileException {
    String path = distributionServiceConfig.getDigitalGreenCertificate().getDiseaseAgentTargetedJsonPath();
    return readConfiguredJsonOrDefault(resourceLoader, path, "dgc/disease-agent-targeted.json",
        ValueSet.class);
  }

  /**
   * Read the JSON for the Rapid Antigen Test name and manufacturer.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readTestManfJson() throws UnableToLoadFileException {
    String path = distributionServiceConfig.getDigitalGreenCertificate().getTestManfJsonPath();
    return readConfiguredJsonOrDefault(resourceLoader, path, "dgc/test-manf.json",
        ValueSet.class);
  }

  /**
   * Read the JSON for the test Result.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readTestResultJson() throws UnableToLoadFileException {
    String path = distributionServiceConfig.getDigitalGreenCertificate().getTestResultJsonPath();
    return readConfiguredJsonOrDefault(resourceLoader, path, "dgc/test-result.json",
        ValueSet.class);
  }

  /**
   * Read the JSON for the type of Test.
   *
   * @return The corresponding JSON object.
   */
  ValueSet readTestTypeJson() throws UnableToLoadFileException {
    String path = distributionServiceConfig.getDigitalGreenCertificate().getTestTypeJsonPath();
    return readConfiguredJsonOrDefault(resourceLoader, path, "dgc/test-type.json",
        ValueSet.class);
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
}

