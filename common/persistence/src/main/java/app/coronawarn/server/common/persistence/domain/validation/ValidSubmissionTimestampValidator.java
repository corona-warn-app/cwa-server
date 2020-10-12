

package app.coronawarn.server.common.persistence.domain.validation;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidSubmissionTimestampValidator
    implements ConstraintValidator<ValidSubmissionTimestamp, Long> {

  public static final long SECONDS_PER_HOUR = TimeUnit.HOURS.toSeconds(1);

  @Override
  public boolean isValid(Long submissionTimestamp, ConstraintValidatorContext constraintValidatorContext) {
    long currentHoursSinceEpoch = Instant.now().getEpochSecond() / SECONDS_PER_HOUR;
    return submissionTimestamp >= 0L && submissionTimestamp <= currentHoursSinceEpoch;
  }
}
