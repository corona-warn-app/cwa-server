

package app.coronawarn.server.services.submission.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
   * Validate if the MaximumRequestSize of the {@link SubmissionServiceConfig} is in the defined range.
   */
  @Override
  public void validate(Object o, Errors errors) {
    SubmissionServiceConfig properties = (SubmissionServiceConfig) o;

    validateMaxRequestSize(errors, properties);
    validateSupportedCountries(errors, properties);
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
