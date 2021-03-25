package app.coronawarn.server.common.persistence.domain.config;

import app.coronawarn.server.common.persistence.utils.YamlPropertySourceFactory;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties()
@PropertySource(value = "classpath:trl-value-mapping-1.15.yaml",
    factory = YamlPropertySourceFactory.class)
public class PreDistributionTrlValueMappingProvider {

  private List<TransmissionRiskValueMapping> transmissionRiskValueMapping;

  public PreDistributionTrlValueMappingProvider() {
  }

  public List<TransmissionRiskValueMapping> getTransmissionRiskValueMapping() {
    return transmissionRiskValueMapping;
  }

  public void setTransmissionRiskValueMapping(
      List<TransmissionRiskValueMapping> transmissionRiskValueMapping) {
    this.transmissionRiskValueMapping = transmissionRiskValueMapping;
  }

  public Iterable<app.coronawarn.server.common.protocols.internal.v2.TransmissionRiskValueMapping>
          getTransmissionRiskValueMappingAsProto() {
    return transmissionRiskValueMapping.stream().map(mapping -> toProto(mapping))
        .collect(Collectors.toList());
  }

  private app.coronawarn.server.common.protocols.internal.v2.TransmissionRiskValueMapping toProto(
      TransmissionRiskValueMapping mapping) {
    return app.coronawarn.server.common.protocols.internal.v2.TransmissionRiskValueMapping
        .newBuilder().setTransmissionRiskLevel(mapping.getTransmissionRiskLevel())
        .setTransmissionRiskValue(mapping.getTransmissionRiskValue()).build();
  }
}
