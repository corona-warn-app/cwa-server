package app.coronawarn.server.services.distribution.dgc;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VaccineProphylaxisJsonStringObject {

  @JsonProperty("valueSetId")
  private String valueSetId;

  @JsonProperty("valueSetDate")
  private Date valueSetDate;

  @JsonProperty("valueSetValues")
  private Map<String, ValueSetObject> valueSetValues;

  public String getValueSetId() {
    return valueSetId;
  }

  public void setValueSetId(String valueSetId) {
    this.valueSetId = valueSetId;
  }

  public Date getValueSetDate() {
    return valueSetDate;
  }

  public void setValueSetDate(Date valueSetDate) {
    this.valueSetDate = valueSetDate;
  }

  public Map<String, ValueSetObject> getValueSetValues() {
    return valueSetValues;
  }

  public void setValueSetValues(Map<String, ValueSetObject> valueSetValues) {
    this.valueSetValues = valueSetValues;
  }
}
