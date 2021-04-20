package app.coronawarn.server.common.persistence.domain.validation;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidSubmissionTypeValidator implements ConstraintValidator<ValidSubmissionType, SubmissionType> {

  @Override
  public boolean isValid(SubmissionType submissionType, ConstraintValidatorContext constraintValidatorContext) {
    return SubmissionType.SUBMISSION_TYPE_PCR_TEST.equals(submissionType)
        || SubmissionType.SUBMISSION_TYPE_RAPID_TEST.equals(submissionType);
  }
}
