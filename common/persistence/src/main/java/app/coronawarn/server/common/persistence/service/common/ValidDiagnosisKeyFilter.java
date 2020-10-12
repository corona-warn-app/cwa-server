

package app.coronawarn.server.common.persistence.service.common;


import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ValidDiagnosisKeyFilter {

  private static final Logger logger = LoggerFactory.getLogger(ValidDiagnosisKeyFilter.class);

  /**
   * Rerturns a subset of diagnosis keys from the given list which have
   * passed the default entity validation.
   */
  public List<DiagnosisKey> filter(List<DiagnosisKey> diagnosisKeys) {
    List<DiagnosisKey> validDiagnosisKeys =
        diagnosisKeys.stream().filter(this::isDiagnosisKeyValid).collect(Collectors.toList());

    int numberOfDiscardedKeys = diagnosisKeys.size() - validDiagnosisKeys.size();
    logger.info("Retrieved {} diagnosis key(s). Discarded {} diagnosis key(s) from the result as invalid.",
        diagnosisKeys.size(), numberOfDiscardedKeys);

    return validDiagnosisKeys;
  }

  /**
   * Returns true if the given diagnosis key has passed the default entity validation.
   */
  public boolean isDiagnosisKeyValid(DiagnosisKey diagnosisKey) {
    Collection<ConstraintViolation<DiagnosisKey>> violations = diagnosisKey.validate();
    boolean isValid = violations.isEmpty();

    if (!isValid) {
      List<String> violationMessages =
          violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
      logger.warn("Validation failed for diagnosis key from database. Violations: {}", violationMessages);
    }

    return isValid;
  }

}
