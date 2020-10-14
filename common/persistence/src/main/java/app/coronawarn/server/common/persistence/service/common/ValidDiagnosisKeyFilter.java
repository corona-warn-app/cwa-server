
package app.coronawarn.server.common.persistence.service.common;

import static app.coronawarn.server.common.persistence.service.common.PersistenceLogMessages.NR_RETRIEVED_DISCARDED_DIAGNOSIS_KEYS;
import static app.coronawarn.server.common.persistence.service.common.PersistenceLogMessages.VALIDATION_FAILED_WITH_VIOLATIONS;

import app.coronawarn.server.common.Logger;
import app.coronawarn.server.common.LoggerFactory;
import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import org.springframework.stereotype.Component;

@Component
public class ValidDiagnosisKeyFilter {

  private static final Logger logger = LoggerFactory.getLogger(ValidDiagnosisKeyFilter.class);

  /**
   * Returns a subset of diagnosis keys from the given list which have
   * passed the default entity validation.
   */
  public List<DiagnosisKey> filter(List<DiagnosisKey> diagnosisKeys) {
    List<DiagnosisKey> validDiagnosisKeys =
        diagnosisKeys.stream().filter(this::isDiagnosisKeyValid).collect(Collectors.toList());

    int numberOfDiscardedKeys = diagnosisKeys.size() - validDiagnosisKeys.size();
    logger.info(NR_RETRIEVED_DISCARDED_DIAGNOSIS_KEYS, diagnosisKeys.size(), numberOfDiscardedKeys);

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
      logger.warn(VALIDATION_FAILED_WITH_VIOLATIONS, violationMessages);
    }

    return isValid;
  }

}
