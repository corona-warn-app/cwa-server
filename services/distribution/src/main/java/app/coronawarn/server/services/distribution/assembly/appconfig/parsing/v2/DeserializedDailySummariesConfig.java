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
  public void setAttenuationBucketThresholdDb(List<Integer> attenuationBucketThresholdDb) {
    this.attenuationBucketThresholdDb = attenuationBucketThresholdDb;
  }
  public List<Double> getAttenuationBucketWeights() {
    return attenuationBucketWeights;
  }
  public void setAttenuationBucketWeights(List<Double> attenuationBucketWeights) {
    this.attenuationBucketWeights = attenuationBucketWeights;
  }
  public Integer getDaysSinceExposureThreshold() {
    return daysSinceExposureThreshold;
  }
  public void setDaysSinceExposureThreshold(Integer daysSinceExposureThreshold) {
    this.daysSinceExposureThreshold = daysSinceExposureThreshold;
  }
  public Map<Integer, Double> getInfectiousnessWeights() {
    return infectiousnessWeights;
  }
  public void setInfectiousnessWeights(Map<Integer, Double> infectiousnessWeights) {
    this.infectiousnessWeights = infectiousnessWeights;
  }
  public Integer getMinimumWindowScore() {
    return minimumWindowScore;
  }
  public void setMinimumWindowScore(Integer minimumWindowScore) {
    this.minimumWindowScore = minimumWindowScore;
  }
  public Map<Integer, Double> getReportTypeWeights() {
    return reportTypeWeights;
  }
  public void setReportTypeWeights(Map<Integer, Double> reportTypeWeights) {
    this.reportTypeWeights = reportTypeWeights;
  }
}
