package app.coronawarn.server.services.distribution.dgc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessRule {

  private List<String> affectedFields;
  private String certificateType;
  private String country;
  private List<Description> description;
  private String engine;
  private String engineVersion;
  private String identifier;
  private Object logic;
  private String schemaVersion;
  private String type;
  private String validFrom;
  private String validTo;
  private String version;

  public String getIdentifier() {
    return identifier;
  }

  @JsonProperty("Identifier")
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getVersion() {
    return version;
  }

  @JsonProperty("Version")
  public void setVersion(String version) {
    this.version = version;
  }

  public String getCountry() {
    return country;
  }

  @JsonProperty("Country")
  public void setCountry(String country) {
    this.country = country;
  }

  public String getType() {
    return type;
  }

  @JsonProperty("Type")
  public void setType(String type) {
    this.type = type;
  }

  public String getSchemaVersion() {
    return schemaVersion;
  }

  @JsonProperty("SchemaVersion")
  public void setSchemaVersion(String schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  public String getEngine() {
    return engine;
  }

  @JsonProperty("Engine")
  public void setEngine(String engine) {
    this.engine = engine;
  }

  public String getEngineVersion() {
    return engineVersion;
  }

  @JsonProperty("EngineVersion")
  public void setEngineVersion(String engineVersion) {
    this.engineVersion = engineVersion;
  }

  public String getCertificateType() {
    return certificateType;
  }

  @JsonProperty("CertificateType")
  public void setCertificateType(String certificateType) {
    this.certificateType = certificateType;
  }

  public List<Description> getDescription() {
    return description;
  }

  @JsonProperty("Description")
  public void setDescription(List<Description> description) {
    this.description = description;
  }

  public String getValidFrom() {
    return validFrom;
  }

  @JsonProperty("ValidFrom")
  public void setValidFrom(String validFrom) {
    this.validFrom = validFrom;
  }

  public String getValidTo() {
    return validTo;
  }

  @JsonProperty("ValidTo")
  public void setValidTo(String validTo) {
    this.validTo = validTo;
  }

  public List<String> getAffectedFields() {
    return affectedFields;
  }

  @JsonProperty("AffectedFields")
  public void setAffectedFields(List<String> affectedFields) {
    this.affectedFields = affectedFields;
  }

  @JsonProperty("Logic")
  public Object getLogic() {
    return logic;
  }

  public void setLogic(Object logic) {
    this.logic = logic;
  }

  public static class Description {
    private String desc;
    private String lang;

    public String getLang() {
      return lang;
    }

    public void setLang(String lang) {
      this.lang = lang;
    }

    public String getDesc() {
      return desc;
    }

    public void setDesc(String desc) {
      this.desc = desc;
    }
  }

  public enum RuleType {
    Acceptance,
    Invalidation,
    BoosterNotification
  }

}
