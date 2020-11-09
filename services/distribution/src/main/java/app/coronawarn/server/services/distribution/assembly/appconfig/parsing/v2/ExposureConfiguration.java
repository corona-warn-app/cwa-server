package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import java.util.List;
import java.util.Map;

public class ExposureConfiguration {
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



}
