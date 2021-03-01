package app.coronawarn.server.services.callback.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "services.callback")
@Validated
public class CallbackServiceConfig {

  private String efgsCertCn;

  private boolean registerOnStartup;

  private String endpointUrl;

  public String getEfgsCertCn() {
    return efgsCertCn;
  }

  public void setEfgsCertCn(String efgsCertCn) {
    this.efgsCertCn = efgsCertCn;
  }

  public boolean isRegisterOnStartup() {
    return registerOnStartup;
  }

  public void setRegisterOnStartup(boolean registerOnStartup) {
    this.registerOnStartup = registerOnStartup;
  }

  public String getEndpointUrl() {
    return endpointUrl;
  }

  public void setEndpointUrl(String endpointUrl) {
    this.endpointUrl = endpointUrl;
  }
}
