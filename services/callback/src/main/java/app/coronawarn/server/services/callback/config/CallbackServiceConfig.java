package app.coronawarn.server.services.callback.config;

import app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "services.callback")
@Validated
public class CallbackServiceConfig {

  private String certCn;

  private boolean registerOnStartup;

  private String endpointUrl;

  private FederationBatchSourceSystem sourceSystem;

  public FederationBatchSourceSystem getSourceSystem() {
    return sourceSystem;
  }

  public void setSourceSystem(FederationBatchSourceSystem sourceSystem) {
    this.sourceSystem = sourceSystem;
  }

  public String getCertCn() {
    return certCn;
  }

  public void setCertCn(String certCn) {
    this.certCn = certCn;
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
