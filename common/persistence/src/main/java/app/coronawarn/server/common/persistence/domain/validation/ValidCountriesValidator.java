

package app.coronawarn.server.common.persistence.domain.validation;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidCountriesValidator implements ConstraintValidator<ValidCountries, Set<String>> {

  private static final List<String> ISO_COUNTRIES = List.of(Locale.getISOCountries());

  @Override
  public boolean isValid(Set<String> countries, ConstraintValidatorContext constraintValidatorContext) {
    return ISO_COUNTRIES.containsAll(countries);
  }
}
