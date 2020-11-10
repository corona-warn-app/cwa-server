package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import java.util.List;
import java.util.Map;

public class DeserializedDailySummariesConfig {

  private List<Integer> attenuationBucketThresholdDb;
  private List<Double> attenuationBucketWeights;
  private Integer daysSinceExposureThreshold;

  private Map<Integer, Double> infectiousnessWeights;
  private Integer minimumWindowScore;
  private Map<Integer, Double> reportTypeWeights;

  public List<Integer> getAttenuationBucketThresholdDb() {
    return attenuationBucketThresholdDb;
  }

  public List<Double> getAttenuationBucketWeights() {
    return attenuationBucketWeights;
  }

  public Integer getDaysSinceExposureThreshold() {
    return daysSinceExposureThreshold;
  }

  public Map<Integer, Double> getInfectiousnessWeights() {
    return infectiousnessWeights;
  }

  public Integer getMinimumWindowScore() {
    return minimumWindowScore;
  }

  public Map<Integer, Double> getReportTypeWeights() {
    return reportTypeWeights;
  }

}
