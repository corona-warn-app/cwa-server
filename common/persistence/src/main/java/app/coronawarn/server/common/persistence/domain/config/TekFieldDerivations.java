package app.coronawarn.server.common.persistence.domain.config;

import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

  @NotNull
  @NotEmpty
  private Map<Integer, Integer> dsosFromTrl;

  @NotNull
  @NotEmpty
  private Map<Integer, Integer> trlFromDsos;

  public Map<Integer, Integer> getDsosFromTrl() {
    return dsosFromTrl;
  }

  public void setDsosFromTrl(Map<Integer, Integer> dsosFromTrl) {
    this.dsosFromTrl = dsosFromTrl;
  }

  public Map<Integer, Integer> getTrlFromDsos() {
    return trlFromDsos;
  }

  public void setTrlFromDsos(Map<Integer, Integer> trlFromDsos) {
    this.trlFromDsos = trlFromDsos;
  }

  public Integer deriveDsosFromTrl(Integer trlValue) {
    // the derivation logic is subject to refinement
    return dsosFromTrl.getOrDefault(trlValue, 0);
  }

  public Integer deriveTrlFromDsos(Integer dsosValue) {
    // the derivation logic is subject to refinement
    return trlFromDsos.getOrDefault(dsosValue, 1);
  }
}
