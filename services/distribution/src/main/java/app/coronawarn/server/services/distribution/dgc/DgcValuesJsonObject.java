package app.coronawarn.server.services.distribution.dgc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DgcValuesJsonObject {

  @JsonProperty("language")
  private Language language;

  @JsonProperty("vp")
  Map<String, String> vp;

  @JsonProperty("mp")
  Map<String, String> mp;

  @JsonProperty("ma")
  Map<String, String> ma;
}
