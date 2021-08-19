package app.coronawarn.server.services.submission.validation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.services.submission.checkins.EventCheckInProtectedReportsValidator;
import app.coronawarn.server.services.submission.checkins.EventCheckinDataValidator;
import app.coronawarn.server.services.submission.validation.ValidSubmissionOnBehalfPayload.ValidSubmissionOnBehalfPayloadValidator;
import app.coronawarn.server.services.submission.validation.ValidSubmissionPayload.SubmissionPayloadValidator;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.stream.Stream;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Target(PARAMETER)
@Retention(RUNTIME)
@Constraint(validatedBy = ValidSubmissionOnBehalfPayloadValidator.class)
public @interface ValidSubmissionOnBehalfPayload {

  /**
   * Error message.
   *
   * @return the error message
   */
  String message() default "Invalid submission on behalf payload.";

  /**
   * Groups.
   *
   * @return generic array
   */
  Class<?>[] groups() default {};

  /**
   * Payload.
   *
   * @return generic array that extends {@link Payload}
   */
  Class<? extends Payload>[] payload() default {};

  class ValidSubmissionOnBehalfPayloadValidator implements
      ConstraintValidator<ValidSubmissionOnBehalfPayload, SubmissionPayload> {

    /**
     * EventCheckinDataValidator.
     *
     * @deprecated in favor of {@link #eventCheckInProtectedReportsValidator}.
     */
    @Deprecated(since = "2.8", forRemoval = true)
    private final EventCheckinDataValidator eventCheckInValidator;
    private final EventCheckInProtectedReportsValidator eventCheckInProtectedReportsValidator;
    private static final Logger logger = LoggerFactory.getLogger(SubmissionPayloadValidator.class);
    private static final Marker SECURITY = MarkerFactory.getMarker("SECURITY");

    public ValidSubmissionOnBehalfPayloadValidator(EventCheckinDataValidator eventCheckinValidator,
        EventCheckInProtectedReportsValidator eventCheckInProtectedReportsValidator) {
      this.eventCheckInValidator = eventCheckinValidator;
      this.eventCheckInProtectedReportsValidator = eventCheckInProtectedReportsValidator;
    }


    /**
     * Checks whether a submission on behalf is valid or not. The following constraints need to be enforced.
     * <p>`keys` must be an empty set</p>
     * <p>`visitedCountries` must be an empty set</p>
     * <p>`consentToFederation` must be `false`</p>
     * <p>`submissionType` must be `SUBMISSION_TYPE_HOST_WARNING`</p>
     * <p>`checkIns` must contain items that all share the same `locationId` (note that `checkIns` may be empty)</p>
     * <p>`checkInProtectedReports` must not be empty</p>
     * <p>`checkInProtectedReports` must contain items that all share the same `locationIdHash`</p>
     *
     * @param submissionPayload the submission on behalf that will be verified.
     * @param context           constraint validator context for enabling violation messages.
     * @return whether the payload is valid or not.
     */
    @Override
    public boolean isValid(SubmissionPayload submissionPayload, ConstraintValidatorContext context) {
      return Stream.of(diagnosisKeysAreEmpty(submissionPayload, context),
          visitedCountriesAreEmpty(submissionPayload, context),
          consentToFederationIsFalse(submissionPayload, context),
          submissionTypeIsHostWarning(submissionPayload, context),
          eventCheckInValidator.verify(submissionPayload, context),
          eventCheckInValidator.verifyHaveSameLocationId(submissionPayload, context),
          protectedCheckInsAreNotEmpty(submissionPayload, context),
          eventCheckInProtectedReportsValidator.verifyHaveSameLocationIdHash(submissionPayload, context),
          eventCheckInProtectedReportsValidator.verify(submissionPayload, context))
          .allMatch(it -> it.equals(Boolean.TRUE));
    }

    private boolean diagnosisKeysAreEmpty(SubmissionPayload submissionPayload,
        ConstraintValidatorContext context) {
      if (!submissionPayload.getKeysList().isEmpty()) {
        final String warningMessage = "Keys must be an empty set.";
        logger.warn(SECURITY, warningMessage);
        addViolation(context, warningMessage);
        return false;
      }
      return true;
    }

    private boolean visitedCountriesAreEmpty(SubmissionPayload submissionPayload,
        ConstraintValidatorContext context) {
      if (!submissionPayload.getVisitedCountriesList().isEmpty()) {
        logger.warn(SECURITY, "Visited countries must be an empty set.");
        addViolation(context, "Visited countries must be an empty set.");
        return false;
      }
      return true;
    }

    private boolean consentToFederationIsFalse(SubmissionPayload submissionPayload,
        ConstraintValidatorContext context) {
      if (submissionPayload.getConsentToFederation()) {
        logger.warn(SECURITY, "Consent to federation must be false.");
        addViolation(context, "Consent to federation must be false.");
        return false;
      }
      return true;
    }

    private boolean submissionTypeIsHostWarning(SubmissionPayload submissionPayload,
        ConstraintValidatorContext context) {
      if (!submissionPayload.getSubmissionType().equals(SubmissionType.SUBMISSION_TYPE_HOST_WARNING)) {
        logger.warn(SECURITY, "Submission type must be host warning.");
        addViolation(context, "Submission type must be host warning.");
        return false;
      }
      return true;
    }

    private boolean protectedCheckInsAreNotEmpty(SubmissionPayload submissionPayload,
        ConstraintValidatorContext context) {
      if (submissionPayload.getCheckInProtectedReportsList().isEmpty()) {
        logger.warn(SECURITY, "Checkin protected reports must not be empty.");
        addViolation(context, "Checkin protected reports must not be empty.");
        return false;
      }
      return true;
    }

    private void addViolation(ConstraintValidatorContext validatorContext, String message) {
      validatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

  }
}
