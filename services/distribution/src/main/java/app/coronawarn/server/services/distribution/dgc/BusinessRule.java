package app.coronawarn.server.services.distribution.dgc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  /**
   * Filters all older {@link BusinessRule}s and keeps only one for each available major version.
   *
   * @param rules to be filtered
   * @return {@link Map} with the major version as key
   */
  public static Map<Integer, Collection<BusinessRule>> filterAndSort(final Collection<BusinessRule> rules) {
    final Map<Integer, Collection<BusinessRule>> result = new HashMap<>();

    // group BusinessRule by Identifier
    final Map<String, Collection<BusinessRule>> identifiers = new HashMap<>();
    rules.forEach(r -> {
      final Collection<BusinessRule> list = identifiers.computeIfAbsent(r.getIdentifier(), k -> new ArrayList<>());
      list.add(r);
    });

    // per same Identifier
    identifiers.keySet().forEach(identifier -> {
      // find latest version by it's Major
      final Map<Integer, BusinessRule> map = groupByMajor(identifiers.get(identifier));
      map.forEach((major, rule) -> {
        // add to result per it's Major
        Collection<BusinessRule> merge = result.computeIfAbsent(major, k -> new ArrayList<>());
        merge.add(rule);
      });
    });

    return result;
  }

  static Map<Integer, BusinessRule> groupByMajor(final Collection<BusinessRule> rules) {
    final Map<Integer, BusinessRule> result = new HashMap<>();
    for (final BusinessRule rule : rules) {
      final Version version = rule.version();
      result.merge(version.getMajorVersion(), rule, (r1, r2) -> r1.isSameMajorVersionButNewer(r2) ? r1 : r2);
    }
    return result;
  }

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

  Version version() {
    return VersionUtil.parseVersion(getVersion(), null, null);
  }

  boolean isSameMajorVersionButNewer(final BusinessRule other) {
    return version().getMajorVersion() == other.version().getMajorVersion()
        && version().compareTo(other.version()) >= 0;
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
    ACCEPTANCE("Acceptance"),
    INVALIDATION("Invalidation"),
    BOOSTER_NOTIFICATION("BoosterNotification"),
    COMMON_COVID_LOGIC("CCLConfiguration");

    private final String type;

    private RuleType(String type) {
      this.type = type;
    }

    public String getType() {
      return type;
    }
  }
}
