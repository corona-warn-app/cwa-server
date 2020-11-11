

package app.coronawarn.server.services.distribution.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validate the values of the DistributionServiceConfig.
 */
public class DistributionServiceConfigValidator implements Validator {

  private static final List<String> ISO_COUNTRIES = Arrays.asList(Locale.getISOCountries());

  @Override
  public boolean supports(Class<?> type) {
    return type == DistributionServiceConfig.class;
  }

  /**
   * Validate the {@link DistributionServiceConfig} if the supported country parameter is conform to the ISO 3116.
   */
  @Override
  public void validate(Object o, Errors errors) {
    DistributionServiceConfig properties = (DistributionServiceConfig) o;
    checkSupportedCountries(errors, properties);
    checkAndroidVersionCodes(errors, properties);
  }

  private void checkAndroidVersionCodes(Errors errors, DistributionServiceConfig properties) {
    if (properties.getAppVersions().getLatestAndroidVersionCode() < 0) {
      errors.rejectValue("appVersions.latestAndroidVersionCode", "",
          "Android Version Code should be positive or zero");
    }

    if (properties.getAppVersions().getMinAndroidVersionCode() < 0) {
      errors.rejectValue("appVersions.minAndroidVersionCode", "",
          "Android Version Code should be positive or zero");
    }
  }

  private void checkSupportedCountries(Errors errors, DistributionServiceConfig properties) {
    Arrays.stream(properties.getSupportedCountries()).forEach(country -> {
      if (!ISO_COUNTRIES.contains(country)) {
        errors.rejectValue("supportedCountries",
            "[" + country + "]: Country code isn't compliant to ISO 3166.");
      }
    });
  }
}
