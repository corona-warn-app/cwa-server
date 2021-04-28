package app.coronawarn.server.services.distribution.dgc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValueSetObject {

  @JsonProperty("display")
  private String display;

  @JsonProperty("lang")
  private Language lang;

  @JsonProperty("active")
  private boolean active;

  @JsonProperty("version")
  private String version;

  @JsonProperty("system")
  private String system;

  @JsonProperty("valueSetId")
  private String valueSetId;
}
