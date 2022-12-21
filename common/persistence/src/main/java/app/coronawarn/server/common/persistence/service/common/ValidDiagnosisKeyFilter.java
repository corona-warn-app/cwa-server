package app.coronawarn.server.common.persistence.service.common;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.util.Collection;
import javax.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ValidDiagnosisKeyFilter {

  private static final Logger logger = LoggerFactory.getLogger(ValidDiagnosisKeyFilter.class);

  /**
   * Returns a subset of diagnosis keys from the given list which have passed the default entity validation.
   *
   * @param diagnosisKeys list of DiagnosisKey
   * @return list of valid DiagnosisKey
   */
  public Collection<DiagnosisKey> filter(final Collection<DiagnosisKey> diagnosisKeys) {
    Collection<DiagnosisKey> validDiagnosisKeys = diagnosisKeys.stream().filter(this::isDiagnosisKeyValid).toList();

    int numberOfDiscardedKeys = diagnosisKeys.size() - validDiagnosisKeys.size();
    logger.info("Retrieved {} diagnosis key(s). Discarded {} diagnosis key(s) from the result as invalid.",
        diagnosisKeys.size(), numberOfDiscardedKeys);

    return validDiagnosisKeys;
  }

  /**
   * Returns true if the given diagnosis key has passed the default entity validation.
   *
   * @param diagnosisKey a DiagnosisKey
   * @return boolean value to indicate if the DiagnosisKey is valid
   */
  public boolean isDiagnosisKeyValid(DiagnosisKey diagnosisKey) {
    final Collection<ConstraintViolation<DiagnosisKey>> violations = diagnosisKey.validate();
    boolean isValid = violations.isEmpty();

    if (!isValid) {
      final Collection<String> violationMessages = violations.stream().map(ConstraintViolation::getMessage).toList();
      logger.warn("Validation failed for diagnosis key from database. Violations: {}", violationMessages);
    }
    return isValid;
  }
}
