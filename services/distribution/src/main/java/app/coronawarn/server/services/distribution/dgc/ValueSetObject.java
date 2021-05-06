package app.coronawarn.server.services.distribution.dgc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValueSetObject {

  @JsonProperty("display")
  private String display;

  @JsonProperty("lang")
  private String lang;

  @JsonProperty("active")
  private boolean active;

  @JsonProperty("version")
  private String version;

  @JsonProperty("system")
  private String system;

  @JsonProperty("valueSetId")
  private String valueSetId;

  public String getDisplay() {
    return display;
  }

  public void setDisplay(String display) {
    this.display = display;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getSystem() {
    return system;
  }

  public void setSystem(String system) {
    this.system = system;
  }

  public String getValueSetId() {
    return valueSetId;
  }

  public void setValueSetId(String valueSetId) {
    this.valueSetId = valueSetId;
  }
}
