package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import java.util.List;
import java.util.Map;

public class DeserializedExposureConfiguration {

  private Map<Integer,Integer> infectiousnessForDaysSinceOnsetOfSymptoms;
  private Integer reportTypeNoneMap;
  private List<Integer> attenuationDurationThresholds;
  private Double immediateDurationWeight;
  private Double mediumDurationWeight;
  private Double nearDurationWeight;
  private Double otherDurationWeight;
  private Integer daysSinceLastExposureThreshold;
  private Double infectiousnessStandardWeight;
  private Double infectiousnessHighWeight;
  private Double reportTypeConfirmedTestWeight;
  private Double reportTypeConfirmedClinicalDiagnosisWeight;
  private Double reportTypeSelfReportedWeight;
  private Double reportTypeRecursiveWeight;

  public Map<Integer, Integer> getInfectiousnessForDaysSinceOnsetOfSymptoms() {
    return infectiousnessForDaysSinceOnsetOfSymptoms;
  }

  public Integer getReportTypeNoneMap() {
    return reportTypeNoneMap;
  }

  public List<Integer> getAttenuationDurationThresholds() {
    return attenuationDurationThresholds;
  }

  public Double getImmediateDurationWeight() {
    return immediateDurationWeight;
  }

  public Double getMediumDurationWeight() {
    return mediumDurationWeight;
  }

  public Double getNearDurationWeight() {
    return nearDurationWeight;
  }

  public Double getOtherDurationWeight() {
    return otherDurationWeight;
  }

  public Integer getDaysSinceLastExposureThreshold() {
    return daysSinceLastExposureThreshold;
  }

  public Double getInfectiousnessStandardWeight() {
    return infectiousnessStandardWeight;
  }

  public Double getInfectiousnessHighWeight() {
    return infectiousnessHighWeight;
  }

  public Double getReportTypeConfirmedTestWeight() {
    return reportTypeConfirmedTestWeight;
  }

  public Double getReportTypeConfirmedClinicalDiagnosisWeight() {
    return reportTypeConfirmedClinicalDiagnosisWeight;
  }

  public Double getReportTypeSelfReportedWeight() {
    return reportTypeSelfReportedWeight;
  }

  public Double getReportTypeRecursiveWeight() {
    return reportTypeRecursiveWeight;
  }

}
