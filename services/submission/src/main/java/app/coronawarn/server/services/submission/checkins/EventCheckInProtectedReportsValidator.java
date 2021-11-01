package app.coronawarn.server.services.submission.checkins;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import java.util.List;
import javax.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class EventCheckInProtectedReportsValidator {

  public static final int INIT_VECTOR_LENGTH = 16;
  public static final int LOCATION_ID_HASH_LENGTH = 32;
  public static final int MAC_LENGTH = 32;
  public static final int ENCRYPTED_CHECK_IN_RECORD_LENGTH = 16;


  private static final Logger logger = LoggerFactory.getLogger(EventCheckInProtectedReportsValidator.class);
  private static final Marker SECURITY = MarkerFactory.getMarker("SECURITY");

  /**
   * Given the submission payload, it verifies whether user event checkInProtectedReports data is aligned with the
   * application constraints. For each checkInProtectedReports:
   * <li>locationIdHash must have 32 bytes</li>
   * <li>iv must have 32 bytes</li>
   * <li>encryptedCheckInRecord must have more than 0 bytes</li>
   */
  public boolean verify(SubmissionPayload submissionPayload, ConstraintValidatorContext validatorContext) {
    List<CheckInProtectedReport> checkInProtectedReportsList = submissionPayload.getCheckInProtectedReportsList();
    return checkInProtectedReportsList.stream()
        .map(checkInProtectedReport -> verifyLocationIdHashLength(checkInProtectedReport, validatorContext)
            && verifyIvLength(checkInProtectedReport, validatorContext)
            && verifyMacLength(checkInProtectedReport, validatorContext)
            && verifyEncryptedCheckInRecordLength(checkInProtectedReport, validatorContext))
        .allMatch(checkInValidation -> checkInValidation.equals(Boolean.TRUE));
  }

  /**
   * Given a submission in the context of host warnings (submission on behalf) check if all protected checkins share the
   * same location id.
   *
   * @param submissionPayload the submission payload to validate.
   * @param context           the validator context for keeping track of the constrain violations.
   * @return whether the protected checkins of this submission payload contain all the same location id.
   */
  public boolean verifyHaveSameLocationIdHash(SubmissionPayload submissionPayload, ConstraintValidatorContext context) {
    final boolean checkInsHaveSameLocationId =
        submissionPayload.getCheckInProtectedReportsList().stream().map(CheckInProtectedReport::getLocationIdHash)
            .distinct().count() == 1;
    if (!checkInsHaveSameLocationId) {
      logger.warn(SECURITY, "Checkins protected reports must contain items that all share the same location id");
      addViolation(context, "Checkins protected reports must contain items that all share the same location id hash.");
    }
    return checkInsHaveSameLocationId;
  }

  boolean verifyLocationIdHashLength(CheckInProtectedReport checkInProtectedReport,
      ConstraintValidatorContext validatorContext) {
    if (ObjectUtils.isEmpty(checkInProtectedReport.getLocationIdHash())
        || checkInProtectedReport.getLocationIdHash().size() != LOCATION_ID_HASH_LENGTH) {
      addViolation(validatorContext, "CheckInProtectedReports locationIdHash must have 32 bytes not "
          + (checkInProtectedReport.getLocationIdHash() == null ? 0
          : checkInProtectedReport.getLocationIdHash().size()));
      return false;
    }
    return true;
  }

  boolean verifyIvLength(CheckInProtectedReport checkInProtectedReport,
      ConstraintValidatorContext validatorContext) {
    if (ObjectUtils.isEmpty(checkInProtectedReport.getIv())
        || checkInProtectedReport.getIv().size() != INIT_VECTOR_LENGTH) {
      addViolation(validatorContext, "CheckInProtectedReports iv must have 32 bytes not "
          + (checkInProtectedReport.getIv() == null ? 0 : checkInProtectedReport.getIv().size()));
      return false;
    }
    return true;
  }

  boolean verifyMacLength(CheckInProtectedReport checkInProtectedReport,
      ConstraintValidatorContext validatorContext) {
    if (ObjectUtils.isEmpty(checkInProtectedReport.getMac())
        || checkInProtectedReport.getMac().size() != MAC_LENGTH) {
      addViolation(validatorContext, "CheckInProtectedReports mac must have 32 bytes not "
          + (checkInProtectedReport.getMac() == null ? 0 : checkInProtectedReport.getMac().size()));
      return false;
    }
    return true;
  }

  boolean verifyEncryptedCheckInRecordLength(CheckInProtectedReport checkInProtectedReport,
      ConstraintValidatorContext validatorContext) {
    if (ObjectUtils.isEmpty(checkInProtectedReport.getEncryptedCheckInRecord())
        || checkInProtectedReport.getEncryptedCheckInRecord().size() != ENCRYPTED_CHECK_IN_RECORD_LENGTH) {
      addViolation(validatorContext, "CheckInProtectedReports encryptedCheckInRecord must have 16 bytes not "
          + (checkInProtectedReport.getEncryptedCheckInRecord() == null ? 0
          : checkInProtectedReport.getEncryptedCheckInRecord().size()));
      return false;
    }
    return true;
  }

  void addViolation(ConstraintValidatorContext validatorContext, String message) {
    validatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }
}
