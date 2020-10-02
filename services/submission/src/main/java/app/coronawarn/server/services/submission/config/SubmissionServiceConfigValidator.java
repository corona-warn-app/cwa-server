

package app.coronawarn.server.services.submission.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validate the values of the SubmissionServiceConfig.
 */
public class SubmissionServiceConfigValidator implements Validator {

  public static final DataSize MIN_MAXIMUM_REQUEST_SIZE = DataSize.ofBytes(280);
  public static final DataSize MAX_MAXIMUM_REQUEST_SIZE = DataSize.ofKilobytes(200);
  private static final List<String> ISO_COUNTRIES = Arrays.asList(Locale.getISOCountries());

  @Override
  public boolean supports(Class<?> type) {
    return type == SubmissionServiceConfig.class;
  }

  /**
   * Validates the following constraints.
   * <ul>
   *   <li>MaximumRequestSize is in the defined range.</li>
   *   <li>List of SupportedCountries contains only valid ISO Codes</li>
   *   <li>Mapping of trl (Transmission Risk Level) to dsos contains only values in allowed range</li>
   *   <li>Mapping of dsos (Days Since Onset of Symptoms) to trl contains only values in allowed range</li>
   * </ul>
   */
  @Override
  public void validate(Object o, Errors errors) {
    SubmissionServiceConfig properties = (SubmissionServiceConfig) o;

    validateMaxRequestSize(errors, properties);
    validateSupportedCountries(errors, properties);
    validateDaysSinceSymptomsDerivationMap(errors, properties);
    validateTransmissionRiskLevelDerivationMap(errors, properties);
  }

  private void validateTransmissionRiskLevelDerivationMap(Errors errors, SubmissionServiceConfig properties) {
    Map<Integer, Integer> dsosFromTrl =  properties.getTekFieldDerivations().getTrlFromDsos();
    dsosFromTrl.forEach((daysSinceOnsetSymptoms, transmissionRiskLevel) -> {
      checkTrlInAllowedRange(transmissionRiskLevel, errors);
      checkDsosInAllowedRange(daysSinceOnsetSymptoms, errors);
    });
  }

  private void validateDaysSinceSymptomsDerivationMap(Errors errors, SubmissionServiceConfig properties) {
    properties.getTekFieldDerivations().getDsosFromTrl().forEach((trl, dsos) -> {
      checkTrlInAllowedRange(trl, errors);
      checkDsosInAllowedRange(dsos, errors);
    });
  }

  private void checkTrlInAllowedRange(Integer transmissionRiskLevel, Errors errors) {
    if (transmissionRiskLevel > 8 || transmissionRiskLevel < 1) {
      errors.rejectValue("tekFieldDerivations",
          "[" + transmissionRiskLevel + "]: transmissionRiskLevel value is not in the allowed range (1 to 8)");
    }
  }

  private void checkDsosInAllowedRange(Integer daysSinceOnsetSymptoms, Errors errors) {
    if (daysSinceOnsetSymptoms > 4000 || daysSinceOnsetSymptoms < -14) {
      errors.rejectValue("tekFieldDerivations",
          "[" + daysSinceOnsetSymptoms + "]: daysSinceOnsetSymptoms value is not in the allowed range (-14 to 4000)");
    }
  }

  private void validateSupportedCountries(Errors errors, SubmissionServiceConfig properties) {
    Arrays.stream(properties.getSupportedCountries()).forEach(country -> {
      if (!ISO_COUNTRIES.contains(country)) {
        errors.rejectValue("supportedCountries",
            "[" + country + "]: Country code isn't compliant to ISO 3166.");
      }
    });
  }

  private void validateMaxRequestSize(Errors errors, SubmissionServiceConfig properties) {
    if (properties.getMaximumRequestSize().compareTo(MIN_MAXIMUM_REQUEST_SIZE) < 0
        || properties.getMaximumRequestSize().compareTo(MAX_MAXIMUM_REQUEST_SIZE) > 0) {
      errors.rejectValue("maximumRequestSize",
          "Must be at least " + MIN_MAXIMUM_REQUEST_SIZE + " and not more than " + MAX_MAXIMUM_REQUEST_SIZE + ".");
    }
  }
}
