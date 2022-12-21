package app.coronawarn.server.common.persistence.domain.validation;

import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_HOST_WARNING;

import app.coronawarn.server.common.persistence.domain.CheckInProtectedReports;
import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validation applied onto the 'submission_type' column at 'diagnosis_key' table.
 */
public class ValidSubmissionTypeValidator implements ConstraintValidator<ValidSubmissionType, SubmissionType> {

  /**
   * In case of a host-warning (aka. on-behalf-of check-in warning), there won't be any {@link DiagnosisKey}s, but only
   * {@link CheckInProtectedReports}. So SUBMISSION_TYPE_HOST_WARNING isn't allowed to be stored in diagnosis_key table.
   */
  @Override
  public boolean isValid(final SubmissionType submissionType,
      final ConstraintValidatorContext constraintValidatorContext) {
    return !SUBMISSION_TYPE_HOST_WARNING.equals(submissionType);
  }
}
