

package app.coronawarn.server.common.persistence.service;

import static app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestampValidator.SECONDS_PER_HOUR;
import static app.coronawarn.server.common.persistence.service.common.PersistenceLogMessages.DELETING_DIAGNOSIS_KEYS_WITH_SUBMISSION_TIMESTAMP_OLDER;
import static app.coronawarn.server.common.persistence.service.common.PersistenceLogMessages.DIAGNOSIS_KEYS_CONFLICTED_WITH_DB_ENTRIES;
import static java.time.ZoneOffset.UTC;
import static org.springframework.data.util.StreamUtils.createStreamFromIterator;

import app.coronawarn.server.common.Logger;
import app.coronawarn.server.common.LoggerFactory;
import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DiagnosisKeyService {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyService.class);
  private final DiagnosisKeyRepository keyRepository;
  private final ValidDiagnosisKeyFilter validationFilter;

  public DiagnosisKeyService(DiagnosisKeyRepository keyRepository, ValidDiagnosisKeyFilter filter) {
    this.keyRepository = keyRepository;
    this.validationFilter = filter;
  }

  /**
   * Persists the specified collection of {@link DiagnosisKey} instances and returns the number of inserted diagnosis
   * keys. If the key data of a particular diagnosis key already exists in the database, this diagnosis key is not
   * persisted.
   *
   * @param diagnosisKeys must not contain {@literal null}.
   * @return Number of successfully inserted diagnosis keys.
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  @Timed
  @Transactional
  public int saveDiagnosisKeys(Collection<DiagnosisKey> diagnosisKeys) {
    int numberOfInsertedKeys = 0;

    for (DiagnosisKey diagnosisKey : diagnosisKeys) {
      boolean keyInsertedSuccessfully = keyRepository.saveDoNothingOnConflict(
          diagnosisKey.getKeyData(), diagnosisKey.getRollingStartIntervalNumber(), diagnosisKey.getRollingPeriod(),
          diagnosisKey.getSubmissionTimestamp(), diagnosisKey.getTransmissionRiskLevel(),
          diagnosisKey.getOriginCountry(), diagnosisKey.getVisitedCountries().toArray(new String[0]),
          diagnosisKey.getReportType().name(), diagnosisKey.getDaysSinceOnsetOfSymptoms(),
          diagnosisKey.isConsentToFederation());

      if (keyInsertedSuccessfully) {
        numberOfInsertedKeys++;
      }
    }

    int conflictingKeys = diagnosisKeys.size() - numberOfInsertedKeys;
    if (conflictingKeys > 0) {
      logger.warn(DIAGNOSIS_KEYS_CONFLICTED_WITH_DB_ENTRIES, conflictingKeys, diagnosisKeys.size());
    }

    return numberOfInsertedKeys;
  }

  /**
   * Returns all valid persisted diagnosis keys, sorted by their submission timestamp.
   */
  public List<DiagnosisKey> getDiagnosisKeys() {
    List<DiagnosisKey> diagnosisKeys = createStreamFromIterator(
        keyRepository.findAll(Sort.by(Direction.ASC, "submissionTimestamp")).iterator()).collect(Collectors.toList());
    return validationFilter.filter(diagnosisKeys);
  }

  /**
   * Deletes all diagnosis key entries which have a submission timestamp that is older than the specified number of
   * days.
   *
   * @param daysToRetain the number of days until which diagnosis keys will be retained.
   * @throws IllegalArgumentException if {@code daysToRetain} is negative.
   */
  @Transactional
  public void applyRetentionPolicy(int daysToRetain) {
    if (daysToRetain < 0) {
      throw new IllegalArgumentException("Number of days to retain must be greater or equal to 0.");
    }

    long threshold = LocalDateTime
        .ofInstant(Instant.now(), UTC)
        .minusDays(daysToRetain)
        .toEpochSecond(UTC) / SECONDS_PER_HOUR;
    int numberOfDeletions = keyRepository.countOlderThan(threshold);
    logger.info(DELETING_DIAGNOSIS_KEYS_WITH_SUBMISSION_TIMESTAMP_OLDER, numberOfDeletions, daysToRetain);
    keyRepository.deleteOlderThan(threshold);
  }
}
