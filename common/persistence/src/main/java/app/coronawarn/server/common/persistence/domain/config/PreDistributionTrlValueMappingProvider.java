package app.coronawarn.server.common.persistence.domain.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import app.coronawarn.server.common.persistence.utils.YamlPropertySourceFactory;

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
}
