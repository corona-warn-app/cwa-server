

package app.coronawarn.server.services.download.config;

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
        properties.getTekFieldDerivations().getTransmissionRiskLevelFromDaysSinceOnsetOfSymptoms();

    transmissionRiskLevelFromDaysSinceOnsetOfSymptoms.forEach((daysSinceOnsetOfSymptoms, transmissionRiskLevel) -> {
      checkTransmissionRiskLevelInAllowedRange(transmissionRiskLevel, errors);
      checkDaysSinceOnsetOfSymptomsInAllowedRange(daysSinceOnsetOfSymptoms, errors);
    });
  }

  private void checkTransmissionRiskLevelInAllowedRange(Integer transmissionRiskLevel, Errors errors) {
    if (transmissionRiskLevel > 8 || transmissionRiskLevel < 1) {
      errors.rejectValue("tekFieldDerivations",
          "[" + transmissionRiskLevel + "]: transmissionRiskLevel value is not in the allowed range (1 to 8)");
    }
  }

  private void checkDaysSinceOnsetOfSymptomsInAllowedRange(Integer daysSinceOnsetOfSymptoms, Errors errors) {
    if (daysSinceOnsetOfSymptoms > 4000 || daysSinceOnsetOfSymptoms < -14) {
      errors.rejectValue("tekFieldDerivations",
          "[" + daysSinceOnsetOfSymptoms
              + "]: daysSinceOnsetOfSymptoms value is not in the allowed range (-14 to 4000)");
    }
  }
}
