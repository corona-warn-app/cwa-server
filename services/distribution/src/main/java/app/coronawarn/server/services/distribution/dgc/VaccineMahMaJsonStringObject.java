package app.coronawarn.server.services.distribution.dgc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VaccineMahMaJsonStringObject {

  @JsonProperty("valueSetId")
  private String valueSetId;

  @JsonProperty("valueSetDate")
  private String valueSetDate;

  @JsonProperty("valueSetValues")
  private Map<String, ValueSetObject> valueSetValues;

  public String getValueSetId() {
    return valueSetId;
  }

  public void setValueSetId(String valueSetId) {
    this.valueSetId = valueSetId;
  }

  public String getValueSetDate() {
    return valueSetDate;
  }

  public void setValueSetDate(String valueSetDate) {
    this.valueSetDate = valueSetDate;
  }

  public Map<String, ValueSetObject> getValueSetValues() {
    return valueSetValues;
  }

  public void setValueSetValues(Map<String, ValueSetObject> valueSetValues) {
    this.valueSetValues = valueSetValues;
  }
}
