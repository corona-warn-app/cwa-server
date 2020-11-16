package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import java.util.Map;

public class DeserializedDiagnosisKeysDataMapping {

  private Map<Integer, Integer> daysSinceOnsetToInfectiousness;
  private Integer infectiousnessWhenDaysSinceOnsetMissing;
  private Integer reportTypeWhenMissing;

  public Map<Integer, Integer> getDaysSinceOnsetToInfectiousness() {
    return daysSinceOnsetToInfectiousness;
  }

  public Integer getInfectiousnessWhenDaysSinceOnsetMissing() {
    return infectiousnessWhenDaysSinceOnsetMissing;
  }

  public Integer getReportTypeWhenMissing() {
    return reportTypeWhenMissing;
  }

}
