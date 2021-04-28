package app.coronawarn.server.services.distribution.dgc;

import app.coronawarn.server.common.protocols.internal.dgc.ValueSets;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.file.JsonFile;
import app.coronawarn.server.services.distribution.statistics.file.JsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.validation.StatisticsJsonValidator;
import app.coronawarn.server.services.distribution.utils.SerializationUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DigitalGreenCertificateToProtobufMapping {

  private static final Logger logger = LoggerFactory.getLogger(DigitalGreenCertificateToProtobufMapping.class);

  private final DistributionServiceConfig distributionServiceConfig;
  private final JsonFileLoader jsonFileLoader;

  public DigitalGreenCertificateToProtobufMapping(DistributionServiceConfig distributionServiceConfig,
      JsonFileLoader jsonFileLoader) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.jsonFileLoader = jsonFileLoader;
  }

  private JsonFile getFile() {
    return this.jsonFileLoader.getFile();
  }

  /**
   * Reads the JSON and fills the VaccineMahMaJsonStringObject.
   *
   * @return the filled VaccineMahMaJsonStringObject.
   * @throws IOException If the JsonFile throws an exception.
   */
  public VaccineMahMaJsonStringObject readVmpJson() throws IOException {

    return SerializationUtils
        .deserializeJson(getFile().getContent(), typeFactory -> typeFactory
            .constructSimpleType(VaccineMahMaJsonStringObject.class, new JavaType[0]));
  }

  /**
   * Create the Protobuf from JSON.
   *
   * @return the protobuf filled with values from JSON.
   */
  @Bean
  public ValueSets constructProtobufMapping() {
    JsonFile file = this.getFile();

    List<StatisticsJsonStringObject> jsonStringObjects = null;
    try {

      VaccineMahMaJsonStringObject vmm = readVmpJson();
      StatisticsJsonValidator validator = new StatisticsJsonValidator();
      jsonStringObjects = new ArrayList<>(validator.validate(jsonStringObjects));

      return ValueSets.newBuilder().build();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}

