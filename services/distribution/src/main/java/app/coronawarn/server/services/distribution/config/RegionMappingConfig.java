package app.coronawarn.server.services.distribution.config;

import static app.coronawarn.server.services.distribution.statistics.local.BuildLocalStatisticsHelper.getFederalStateConfigIndex;

import app.coronawarn.server.common.persistence.utils.YamlPropertySourceFactory;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

/**
 * Wrapper over properties defined in <code>main-config/region-mapping.yaml</code>. It
 * provides convenience methods to find the federal state group based on a federal state code.
 * https://github.com/corona-warn-app/cwa-app-tech-spec/blob/
 * 42e9e4f3c588cd2fd283f904e9f0ccd53a2b83d0/docs/spec/statistics.md#populate-statistics-message
 */
@Configuration
@Validated
@ConfigurationProperties(prefix = "region-mapping")
@PropertySource(value = "classpath:main-config/region-mapping.yaml",
    factory = YamlPropertySourceFactory.class)
public class RegionMappingConfig {

  private Map<Integer, Integer> federalStatesGroups;

  RegionMappingConfig() {
  }

  public Optional<Integer> getFederalStateGroup(int federalStateId) {
    return Optional.ofNullable(
        federalStatesGroups.get(getFederalStateConfigIndex(federalStateId)));
  }

  public Map<Integer, Integer> getFederalStatesGroups() {
    return federalStatesGroups;
  }

  public void setFederalStatesGroups(Map<Integer, Integer> federalStatesGroups) {
    this.federalStatesGroups = federalStatesGroups;
  }
}
