package app.coronawarn.server.common.persistence.domain.validation;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CountryValidator {

  private static final List<String> ISO_COUNTRIES = List.of(Locale.getISOCountries());

  private CountryValidator() {
  }

  /**
   * Checks if the provided country code is a valid ISO country code.
   *
   * @param country Country code of the diagnosis key
   * @return true if the country code is valid
   */
  public static boolean isValidCountryCode(String country) {
    return ISO_COUNTRIES.contains(country);
  }

  /**
   * Checks if the provided country codes are valid ISO country codes.
   *
   * @param countries Visited Country codes of the diagnosis key
   * @return true if the country codes are valid
   */
  public static boolean isValidCountryCodes(Set<String> countries) {
    return ISO_COUNTRIES.containsAll(countries);
  }
}
