package app.coronawarn.server.services.eventregistration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "event-registration")
public class EventRegistrationConfiguration {

  private Integer version;

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }
}
