package app.coronawarn.server.common.persistence.domain.config;

import app.coronawarn.server.common.persistence.utils.YamlPropertySourceFactory;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "trl")
@PropertySource(value = "classpath:trl-derivation.yaml", factory = YamlPropertySourceFactory.class)
public class TrlDerivations {

  private Map<Integer, Integer> trlMapping;

  public TrlDerivations() {
  }

  public Map<Integer, Integer> getTrlMapping() {
    return trlMapping;
  }

  public void setTrlMapping(Map<Integer, Integer> trlMapping) {
    this.trlMapping = trlMapping;
  }

  public int mapFromTrlSubmittedToTrlToStore(int trlSubmitted) {
    return trlMapping.get(trlSubmitted);
  }
}
