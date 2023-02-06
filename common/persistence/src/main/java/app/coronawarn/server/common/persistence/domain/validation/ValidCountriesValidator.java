package app.coronawarn.server.common.persistence.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class ValidCountriesValidator implements ConstraintValidator<ValidCountries, Set<String>> {

  @Override
  public boolean isValid(Set<String> countries, ConstraintValidatorContext constraintValidatorContext) {
    return CountryValidator.isValidCountryCodes(countries);
  }
}
