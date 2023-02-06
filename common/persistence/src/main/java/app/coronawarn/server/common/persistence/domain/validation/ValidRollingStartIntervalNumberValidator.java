

package app.coronawarn.server.common.persistence.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;

public class ValidRollingStartIntervalNumberValidator
    implements ConstraintValidator<ValidRollingStartIntervalNumber, Integer> {

  @Override
  public boolean isValid(Integer rollingStartIntervalNumber, ConstraintValidatorContext constraintValidatorContext) {
    int currentInstant = Math.toIntExact(Instant.now().getEpochSecond() / 600L);
    return rollingStartIntervalNumber > 0 && rollingStartIntervalNumber <= currentInstant;
  }
}
