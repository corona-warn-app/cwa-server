package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.services.distribution.utils.SerializationUtils.deserializeJsonToSimpleType;

import app.coronawarn.server.common.protocols.internal.dgc.ValueSet;
import app.coronawarn.server.common.protocols.internal.dgc.ValueSetItem;
import app.coronawarn.server.common.protocols.internal.dgc.ValueSets;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class DigitalGreenCertificateToProtobufMapping {

  private static final Logger logger = LoggerFactory.getLogger(DigitalGreenCertificateToProtobufMapping.class);

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  private final Map<String, String> vp = new HashMap<>();
  private final Map<String, String> mp = new HashMap<>();
  private final Map<String, String> ma = new HashMap<>();

  /**
   * Read the JSON for the marketing authorization holders.
   *
   * @return The corresponding JSON object.
   */
  public VaccineMahJsonStringObject readMahJson() {
    String path = distributionServiceConfig.getDigitalGreenCertificate().getMahPath();
    return readConfiguredJsonOrDefault(path, "dgc/vaccine-mah.json",
        VaccineMahJsonStringObject.class);
  }

  /**
   * Read the JSON for the medicinal products.
   *
   * @return The corresponding JSON object.
   */
  public VaccineMedicinalProductJsonStringObject readMedicinalProductJson() {
    String path = distributionServiceConfig.getDigitalGreenCertificate().getMedicinalProductsPath();
    return readConfiguredJsonOrDefault(path, "dgc/vaccine-medicinal-product.json",
        VaccineMedicinalProductJsonStringObject.class);
  }

  /**
   * Read the JSON for the prophylaxis.
   *
   * @return The corresponding JSON object.
   */
  public VaccineProphylaxisJsonStringObject readProphylaxisJson() {
    String path = distributionServiceConfig.getDigitalGreenCertificate().getProphylaxisPath();
    return readConfiguredJsonOrDefault(path, "dgc/vaccine-prophylaxis.json",
        VaccineProphylaxisJsonStringObject.class);
  }

  /**
   * Create the Protobuf from JSON.
   *
   * @return the protobuf filled with values from JSON.
   */
  public ValueSets constructProtobufMapping() {
    List<ValueSetItem> mahItems = toValueSetItems(readMahJson().getValueSetValues());
    List<ValueSetItem> productItems = toValueSetItems(readMedicinalProductJson().getValueSetValues());
    List<ValueSetItem> prophylaxisItems = toValueSetItems(readProphylaxisJson().getValueSetValues());

    return ValueSets.newBuilder()
        .setLanguageValue(0)
        .setMa(ValueSet.newBuilder().addAllItems(mahItems).build())
        .setVp(ValueSet.newBuilder().addAllItems(productItems).build())
        .setVp(ValueSet.newBuilder().addAllItems(prophylaxisItems).build())
        .build();
  }

  private List<ValueSetItem> toValueSetItems(Map<String, ValueSetObject> valueSetValues) {
    return valueSetValues.entrySet().stream().map(
        entry -> (ValueSetItem.newBuilder()
            .setKey(entry.getKey())
            .setDisplayText(entry.getValue().getDisplay())).build())
        .collect(Collectors.toList());
  }

  private <T> T readConfiguredJsonOrDefault(String path, String defaultPath, Class<T> rawType) {
    if (!StringUtils.isEmpty(path)) {
      try {
        return deserializeJsonToSimpleType(path, rawType);
      } catch (IOException e) {
        logger.error("Error reading {} from json {}.", rawType.getSimpleName(), path, e);
      }
    }
    try {
      // fallback to default
      return deserializeJsonToSimpleType(defaultPath, rawType);
    } catch (IOException e) {
      logger.error("We could not load the default {}. This shouldn't happen!", defaultPath, e);
      throw new RuntimeException(e);
    }
  }
}

