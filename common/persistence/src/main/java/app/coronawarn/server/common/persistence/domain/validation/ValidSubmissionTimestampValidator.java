package app.coronawarn.server.common.persistence.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class ValidSubmissionTimestampValidator
    implements ConstraintValidator<ValidSubmissionTimestamp, Integer> {

  public static final long SECONDS_PER_HOUR = TimeUnit.HOURS.toSeconds(1);

  @Override
  public boolean isValid(Integer submissionTimestamp, ConstraintValidatorContext constraintValidatorContext) {
    long currentHoursSinceEpoch = Instant.now().getEpochSecond() / SECONDS_PER_HOUR;
    return submissionTimestamp >= 0 && submissionTimestamp <= currentHoursSinceEpoch;
  }
}
