

package app.coronawarn.server.services.download.config;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.util.Map;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validate the values of the DownloadServiceConfig.
 */
public class DownloadServiceConfigValidator implements Validator {

  @Override
  public boolean supports(Class<?> type) {
    return type == DownloadServiceConfig.class;
  }

  @Override
  public void validate(Object configurationObject, Errors errors) {
    DownloadServiceConfig properties = (DownloadServiceConfig) configurationObject;
    validateTransmissionRiskLevelDerivationMap(errors, properties);
  }

  private void validateTransmissionRiskLevelDerivationMap(Errors errors, DownloadServiceConfig properties) {
    Map<Integer, Integer> transmissionRiskLevelFromDaysSinceOnsetOfSymptoms =
        properties.getTekFieldDerivations().getTransmissionRiskLevelFromDaysSinceSymptoms();

    transmissionRiskLevelFromDaysSinceOnsetOfSymptoms.forEach((daysSinceOnsetOfSymptoms, transmissionRiskLevel) -> {
      checkTransmissionRiskLevelInAllowedRange(transmissionRiskLevel, errors);
      checkDaysSinceOnsetOfSymptomsInAllowedRange(daysSinceOnsetOfSymptoms, errors);
    });
  }

  private void checkTransmissionRiskLevelInAllowedRange(Integer transmissionRiskLevel, Errors errors) {
    if (transmissionRiskLevel > DiagnosisKey.MAX_TRANSMISSION_RISK_LEVEL
        || transmissionRiskLevel < DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL) {
      errors.rejectValue("tekFieldDerivations",
          "[" + transmissionRiskLevel
              + "]: transmissionRiskLevel value is not in the allowed range ("
              + DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL + " to "
              + DiagnosisKey.MAX_TRANSMISSION_RISK_LEVEL + ")");
    }
  }

  private void checkDaysSinceOnsetOfSymptomsInAllowedRange(Integer daysSinceOnsetOfSymptoms, Errors errors) {
    if (daysSinceOnsetOfSymptoms > DiagnosisKey.MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS
        || daysSinceOnsetOfSymptoms < DiagnosisKey.MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS) {
      errors.rejectValue("tekFieldDerivations",
          "[" + daysSinceOnsetOfSymptoms
              + "]: daysSinceOnsetOfSymptoms value is not in the allowed range ("
              + DiagnosisKey.MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS + " to "
              + DiagnosisKey.MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS + ")");
    }
  }
}
