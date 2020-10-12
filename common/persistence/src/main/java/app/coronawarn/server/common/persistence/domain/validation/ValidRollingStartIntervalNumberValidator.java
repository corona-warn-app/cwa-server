

package app.coronawarn.server.common.persistence.domain.validation;

import java.time.Instant;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidRollingStartIntervalNumberValidator
    implements ConstraintValidator<ValidRollingStartIntervalNumber, Integer> {

  @Override
  public boolean isValid(Integer rollingStartIntervalNumber, ConstraintValidatorContext constraintValidatorContext) {
    int currentInstant = Math.toIntExact(Instant.now().getEpochSecond() / 600L);
    return rollingStartIntervalNumber > 0 && rollingStartIntervalNumber < currentInstant;
  }
}
