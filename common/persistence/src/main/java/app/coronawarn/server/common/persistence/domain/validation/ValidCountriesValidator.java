package app.coronawarn.server.common.persistence.domain.validation;

import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidCountriesValidator implements ConstraintValidator<ValidCountries, Set<String>> {

  @Override
  public boolean isValid(Set<String> countries, ConstraintValidatorContext constraintValidatorContext) {
    return CountryValidator.isValidCountryCodes(countries);
  }
}
