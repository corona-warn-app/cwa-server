

package app.coronawarn.server.common.persistence.domain.normalization;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;

/**
 * This data structure is just a container for all fields of a
 * {@link DiagnosisKey} to which normalization can be applied.
 */
public final class NormalizableFields {

  private final Integer transmissionRiskLevel;
  private final Integer daysSinceOnsetOfSymptoms;

  private NormalizableFields(Integer transmissionRiskLevel, Integer daysSinceOnsetOfSymptoms) {
    this.transmissionRiskLevel = transmissionRiskLevel;
    this.daysSinceOnsetOfSymptoms = daysSinceOnsetOfSymptoms;
  }

  public Integer getTransmissionRiskLevel() {
    return transmissionRiskLevel;
  }

  public Integer getDaysSinceOnsetOfSymptoms() {
    return daysSinceOnsetOfSymptoms;
  }

  public static NormalizableFields of(Integer transmissionRiskLevel, Integer daysSinceOnsetOfSymptoms) {
    return new NormalizableFields(transmissionRiskLevel, daysSinceOnsetOfSymptoms);
  }
}
