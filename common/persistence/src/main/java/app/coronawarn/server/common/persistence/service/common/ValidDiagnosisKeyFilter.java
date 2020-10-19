

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
    return diagnosisKeys.stream().filter(this::isDiagnosisKeyValid).collect(Collectors.toList());
  }

  /**
   * Returns true if the given diagnosis key has passed the default entity validation.
   */
  public boolean isDiagnosisKeyValid(DiagnosisKey diagnosisKey) {
    Collection<ConstraintViolation<DiagnosisKey>> violations = diagnosisKey.validate();

    return violations.isEmpty();
  }

}
