package app.coronawarn.server.services.eventregistration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "event-registration")
public class EventRegistrationConfiguration {
  private Integer version;
  private Integer saveRetriesLimit;


  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Integer getSaveRetriesLimit() {
    return saveRetriesLimit;
  }

  public void setSaveRetriesLimit(Integer saveRetriesLimit) {
    this.saveRetriesLimit = saveRetriesLimit;
  }

}
