package app.coronawarn.server.services.distribution.dgc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessRule {

  private String type;
  private String schemaVersion;
  private String engine;
  private String engineVersion;
  private String certificateType;
  private List<Description> description;
  private String validFrom;
  private String validTo;
  private List<String> affectedFields;
  private String logic;
  private String identifier;
  private String version;
  private String countryCode;
  private String hash;

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

  public String getCountryCode() {
    return countryCode;
  }

  @JsonProperty("CountryCode")
  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getHash() {
    return hash;
  }

  @JsonProperty("Hash")
  public void setHash(String hash) {
    this.hash = hash;
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

  public String getLogic() {
    return logic;
  }

  @JsonProperty("Logic")
  public void setLogic(String logic) {
    this.logic = logic;
  }

  public static class Description {
    private String lang;
    private String desc;

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
    Invalidation
  }

}
