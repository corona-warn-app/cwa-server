package app.coronawarn.server.common.persistence.domain.config;

import app.coronawarn.server.common.persistence.utils.YamlPropertySourceFactory;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Wrapper over properties defined in derivation-maps.yaml. It provides convenience methods to
 * derive properties from one another.
 */
@Configuration
@ConfigurationProperties(prefix = "tek-field-derivations")
@PropertySource(value = "classpath:derivation-maps.yaml", factory = YamlPropertySourceFactory.class)
public class TekFieldDerivations {

  private Map<Integer, Integer> dsosFromTrl;

  private Map<Integer, Integer> trlFromDsos;

  private Integer defaultTrl;

  TekFieldDerivations() {
  }

  public Map<Integer, Integer> getDaysSinceSymptomsFromTransmissionRiskLevel() {
    return new HashMap<>(dsosFromTrl);
  }

  public Map<Integer, Integer> getTransmissionRiskLevelFromDaysSinceSymptoms() {
    return new HashMap<>(trlFromDsos);
  }

  /**
   * Returns a mapped DSOS value for the given TRL or throws an exception if the TRL is not part of the mapping.
   * @param transmissionRiskLevel value for transmission risk strength
   * @return mapped days since symptoms started value for transmission risk
   */
  public Integer deriveDaysSinceSymptomsFromTransmissionRiskLevel(Integer transmissionRiskLevel) {
    if (!dsosFromTrl.containsKey(transmissionRiskLevel)) {
      throw new IllegalArgumentException("Transmission Risk Level value " + transmissionRiskLevel
          + " is unknown and a " + "DSOS value is not mapped to it.");
    }
    return dsosFromTrl.get(transmissionRiskLevel);
  }

  /**
   * Returns a mapped TRL value for the given DSOS or the configured system default.
   * @param daysSinceSymptoms number of days since symptoms began
   * @return mapped transmission risk level value for duration of days since symptoms began
   */
  public Integer deriveTransmissionRiskLevelFromDaysSinceSymptoms(Integer daysSinceSymptoms) {
    return trlFromDsos.getOrDefault(daysSinceSymptoms, defaultTrl);
  }

  /**
   * Constructs the configuration class from the given mappings.
   * @param dsosFromTrl days since symptoms started from transmission risk level
   * @param trlFromDsos transmission risk level from days since symptoms started
   * @param defaultTrl a default transmission risk level
   * @return configuration of provided attributes {@link TekFieldDerivations}
   */
  public static TekFieldDerivations from(Map<Integer, Integer> dsosFromTrl,
      Map<Integer, Integer> trlFromDsos, Integer defaultTrl) {
    TekFieldDerivations tekFieldDerivations = new TekFieldDerivations();
    tekFieldDerivations.setDefaultTrl(defaultTrl);
    tekFieldDerivations.setDsosFromTrl(dsosFromTrl);
    tekFieldDerivations.setTrlFromDsos(trlFromDsos);
    return tekFieldDerivations;
  }

  /* Setters below are used by Spring to inject the maps loaded from the yaml.
   * For now this should be used because @ConstructorBinding does not work in the current setup. */
  void setDsosFromTrl(Map<Integer, Integer> dsosFromTrl) {
    this.dsosFromTrl = dsosFromTrl;
  }

  void setTrlFromDsos(Map<Integer, Integer> trlFromDsos) {
    this.trlFromDsos = trlFromDsos;
  }

  public void setDefaultTrl(Integer defaultTrl) {
    this.defaultTrl = defaultTrl;
  }
}
