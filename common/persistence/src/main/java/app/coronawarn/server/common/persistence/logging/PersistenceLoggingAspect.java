package app.coronawarn.server.common.persistence.logging;

import static app.coronawarn.server.common.persistence.logging.PersistenceLogMessages.QUERYING_DIAGNOSIS_KEYS_FROM_COUNTRY;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PersistenceLoggingAspect {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * This method is used to log details after filtering valid diagnosis keys using the {@link
   * app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter}.
   *
   * @param initialKeys  parameter passed to the advised method
   * @param filteredKeys returned value of the advised method
   */
  @AfterReturning(pointcut =
      "execution(* app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter.filter(..))"
          + "&& args(initialKeys)",
      returning = "filteredKeys", argNames = "initialKeys,filteredKeys")
  public void afterReturningValidFilteredDiagnosisKeys(List<DiagnosisKey> initialKeys,
      List<DiagnosisKey> filteredKeys) {
    int numberOfDiscardedKeys = initialKeys.size() - filteredKeys.size();
    logger.info("Retrieved {} diagnosis key(s). Discarded {} diagnosis key(s) from the result as invalid.",
        initialKeys.size(), numberOfDiscardedKeys);
  }

  /**
   * This method is used to log the violations of the invalid diagnosis keys. {@link
   * app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter}.
   *
   * @param diagnosisKey the keys which are validated
   * @param isValid      returned value of the advised method
   */
  @AfterReturning(pointcut =
      "execution(* app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter"
          + ".isDiagnosisKeyValid(..))"
          + "&& args(diagnosisKey)",
      returning = "isValid", argNames = "diagnosisKey,isValid")
  public void afterReturningIsDiagnosisKeyValid(DiagnosisKey diagnosisKey,
      boolean isValid) {
    if (!isValid) {
      List<String> violationMessages = diagnosisKey.validate().stream().map(ConstraintViolation::getMessage)
          .collect(Collectors.toList());
      logger.warn("Validation failed for diagnosis key from database. Violations: {}", violationMessages);
    }
  }

  @Before(
      "execution(* app.coronawarn.server.common.persistence.service.DiagnosisKeyService.getDiagnosisKeysByCountry(..))"
          + "&& args(country)")
  public void beforeGetDiagnosisKeyByCountry(String country) {
    logger.debug(QUERYING_DIAGNOSIS_KEYS_FROM_COUNTRY, country);
  }
}
