package app.coronawarn.server.services.callback.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "services.callback")
@Validated
public class CallbackServiceConfig {

  private String efgsCertCn;

  public String getEfgsCertCn() {
    return efgsCertCn;
  }

  public void setEfgsCertCn(String efgsCertCn) {
    this.efgsCertCn = efgsCertCn;
  }
}
