package app.coronawarn.server.common.persistence.domain.config;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
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

  /**
   * Constructs the configuration class after loading the yaml.
   */
  public TekFieldDerivations(@NotNull @NotEmpty Map<Integer, Integer> dsosFromTrl,
      @NotNull @NotEmpty Map<Integer, Integer> trlFromDsos) {
    this.dsosFromTrl = dsosFromTrl;
    this.trlFromDsos = trlFromDsos;
  }

  public Map<Integer, Integer> getDaysSinceSymptomsFromTransmissionRiskLevel() {
    return new HashMap<Integer, Integer>(dsosFromTrl);
  }

  public Map<Integer, Integer> getTransmissionRiskLevelFromDaysSinceSymptoms() {
    return new HashMap<Integer, Integer>(trlFromDsos);
  }

  /**
   * Returns a mapped DSOS value for the given TRL or throw an exception if the TRL is not part of
   * the accepted range.
   */
  public Integer deriveDaysSinceSymptomsFromTransmissionRiskLevel(Integer transmissionRiskLevel) {
    if (!dsosFromTrl.containsKey(transmissionRiskLevel)) {
      throw new IllegalArgumentException("Transmission Risk Level value " + transmissionRiskLevel
          + " is unkown and a " + "DSOS value is not mapped to it.");
    }
    return dsosFromTrl.get(transmissionRiskLevel);
  }

  /**
   * Returns a mapped TRL value for the given DSOS or the configured system default.
   */
  public Integer deriveTransmissionRiskLevelFromDaysSinceSymptoms(Integer daysSinceSymptoms) {
    // todo .. return the system default
    return trlFromDsos.getOrDefault(daysSinceSymptoms, 1);
  }

  /* Setters below are used by Spring to inject the maps loaded from the yaml.
   * For now this should be used because @ConstructorBinding does not work in the current setup. */
  void setDsosFromTrl(Map<Integer, Integer> dsosFromTrl) {
    this.dsosFromTrl = dsosFromTrl;
  }

  void setTrlFromDsos(Map<Integer, Integer> trlFromDsos) {
    this.trlFromDsos = trlFromDsos;
  }
}
