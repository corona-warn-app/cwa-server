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

  public void setInfectiousnessForDaysSinceOnsetOfSymptoms(
      Map<Integer, Integer> infectiousnessForDaysSinceOnsetOfSymptoms) {
    this.infectiousnessForDaysSinceOnsetOfSymptoms = infectiousnessForDaysSinceOnsetOfSymptoms;
  }

  public Integer getReportTypeNoneMap() {
    return reportTypeNoneMap;
  }

  public void setReportTypeNoneMap(Integer reportTypeNoneMap) {
    this.reportTypeNoneMap = reportTypeNoneMap;
  }

  public List<Integer> getAttenuationDurationThresholds() {
    return attenuationDurationThresholds;
  }

  public void setAttenuationDurationThresholds(List<Integer> attenuationDurationThresholds) {
    this.attenuationDurationThresholds = attenuationDurationThresholds;
  }

  public Double getImmediateDurationWeight() {
    return immediateDurationWeight;
  }

  public void setImmediateDurationWeight(Double immediateDurationWeight) {
    this.immediateDurationWeight = immediateDurationWeight;
  }

  public Double getMediumDurationWeight() {
    return mediumDurationWeight;
  }

  public void setMediumDurationWeight(Double mediumDurationWeight) {
    this.mediumDurationWeight = mediumDurationWeight;
  }

  public Double getNearDurationWeight() {
    return nearDurationWeight;
  }

  public void setNearDurationWeight(Double nearDurationWeight) {
    this.nearDurationWeight = nearDurationWeight;
  }

  public Double getOtherDurationWeight() {
    return otherDurationWeight;
  }

  public void setOtherDurationWeight(Double otherDurationWeight) {
    this.otherDurationWeight = otherDurationWeight;
  }

  public Integer getDaysSinceLastExposureThreshold() {
    return daysSinceLastExposureThreshold;
  }

  public void setDaysSinceLastExposureThreshold(Integer daysSinceLastExposureThreshold) {
    this.daysSinceLastExposureThreshold = daysSinceLastExposureThreshold;
  }

  public Double getInfectiousnessStandardWeight() {
    return infectiousnessStandardWeight;
  }

  public void setInfectiousnessStandardWeight(Double infectiousnessStandardWeight) {
    this.infectiousnessStandardWeight = infectiousnessStandardWeight;
  }

  public Double getInfectiousnessHighWeight() {
    return infectiousnessHighWeight;
  }

  public void setInfectiousnessHighWeight(Double infectiousnessHighWeight) {
    this.infectiousnessHighWeight = infectiousnessHighWeight;
  }

  public Double getReportTypeConfirmedTestWeight() {
    return reportTypeConfirmedTestWeight;
  }

  public void setReportTypeConfirmedTestWeight(Double reportTypeConfirmedTestWeight) {
    this.reportTypeConfirmedTestWeight = reportTypeConfirmedTestWeight;
  }

  public Double getReportTypeConfirmedClinicalDiagnosisWeight() {
    return reportTypeConfirmedClinicalDiagnosisWeight;
  }

  public void setReportTypeConfirmedClinicalDiagnosisWeight(Double reportTypeConfirmedClinicalDiagnosisWeight) {
    this.reportTypeConfirmedClinicalDiagnosisWeight = reportTypeConfirmedClinicalDiagnosisWeight;
  }

  public Double getReportTypeSelfReportedWeight() {
    return reportTypeSelfReportedWeight;
  }

  public void setReportTypeSelfReportedWeight(Double reportTypeSelfReportedWeight) {
    this.reportTypeSelfReportedWeight = reportTypeSelfReportedWeight;
  }

  public Double getReportTypeRecursiveWeight() {
    return reportTypeRecursiveWeight;
  }

  public void setReportTypeRecursiveWeight(Double reportTypeRecursiveWeight) {
    this.reportTypeRecursiveWeight = reportTypeRecursiveWeight;
  }
}
