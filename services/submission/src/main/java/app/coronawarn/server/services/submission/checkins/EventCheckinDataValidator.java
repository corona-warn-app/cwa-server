package app.coronawarn.server.services.submission.checkins;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import java.util.List;
import javax.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * Validator to validate constraints for checkins.
 *
 * @deprecated because trace time warnings are being replaced by protected reports.
 */
@Component
@Deprecated(since = "2.8")
public class EventCheckinDataValidator {

  /**
   * Given the submission payload, it verifies whether user event checkin data is alligned with the application
   * constraints. For each checkin:
   * <li>TRL must be between 1 and 8</li>
   * <li>Checkin time must be greater then 0</li>
   * <li>Checkout time must be after checkin time</li>
   */
  public boolean verify(SubmissionPayload submissionPayload, ConstraintValidatorContext validatorContext) {
    List<CheckIn> checkins = submissionPayload.getCheckInsList();
    return checkins.stream()
        .map(checkin -> verifyTransmissionRiskLevel(checkin, validatorContext)
            && verifyLocationIdLength(checkin, validatorContext) && verifyStartIntervalNumber(checkin, validatorContext)
            && verifyEndIntervalNumber(checkin, validatorContext))
        .allMatch(checkinValidation -> Boolean.valueOf(checkinValidation).equals(Boolean.TRUE));
  }

  boolean verifyLocationIdLength(CheckIn checkin, ConstraintValidatorContext validatorContext) {
    if (ObjectUtils.isEmpty(checkin.getLocationId()) || checkin.getLocationId().size() != 32) {
      addViolation(validatorContext, "Checkin locationId must have 32 bytes not "
          + (checkin.getLocationId() == null ? 0 : checkin.getLocationId().size()));
      return false;
    }
    return true;
  }

  boolean verifyEndIntervalNumber(CheckIn checkin, ConstraintValidatorContext validatorContext) {
    int startIntervalNumber = checkin.getStartIntervalNumber();
    int endIntervalNumber = checkin.getEndIntervalNumber();
    if (endIntervalNumber < startIntervalNumber) {
      addViolation(validatorContext, "Checkin endIntervalNumber must be greater than or equal to startIntervalNumber");
      return false;
    }
    return true;
  }

  boolean verifyStartIntervalNumber(CheckIn checkin, ConstraintValidatorContext validatorContext) {
    int startIntervalNumber = checkin.getStartIntervalNumber();
    if (startIntervalNumber <= 0) {
      addViolation(validatorContext, "Checkin startIntervalNumber must be greater than 0");
      return false;
    }
    return true;
  }

  boolean verifyTransmissionRiskLevel(CheckIn checkin, ConstraintValidatorContext validatorContext) {
    int trl = checkin.getTransmissionRiskLevel();
    if (trl < DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL || trl > DiagnosisKey.MAX_TRANSMISSION_RISK_LEVEL) {
      addViolation(validatorContext, "'" + trl + "' is not a valid transmissionRiskLevel value. Value must be between "
          + DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL + "  and " + DiagnosisKey.MAX_TRANSMISSION_RISK_LEVEL);
      return false;
    }
    return true;
  }

  void addViolation(ConstraintValidatorContext validatorContext, String message) {
    validatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }
}
