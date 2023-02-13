package app.coronawarn.server.common.persistence.service;

import static app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestampValidator.SECONDS_PER_HOUR;
import static java.time.LocalDate.now;
import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneOffset.UTC;
import static org.springframework.data.util.StreamUtils.createStreamFromIterator;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Component
public class DiagnosisKeyService {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyService.class);

  /**
   * Calculates epoch seconds in UTC based upon current time and given days.
   *
   * @param daysToRetain offset in days to use for epoch second
   * @return epoch seconds
   */
  public static long daysToSeconds(final int daysToRetain) {
    if (daysToRetain < 0) {
      throw new IllegalArgumentException("Number of days to retain must be greater or equal to 0.");
    }
    return ofInstant(Instant.now(), UTC).minusDays(daysToRetain).toEpochSecond(UTC) / SECONDS_PER_HOUR;
  }

  private final DiagnosisKeyRepository keyRepository;

  private final ValidDiagnosisKeyFilter validationFilter;

  public DiagnosisKeyService(final DiagnosisKeyRepository keyRepository, final ValidDiagnosisKeyFilter filter) {
    this.keyRepository = keyRepository;
    validationFilter = filter;
  }

  /**
   * Deletes all diagnosis key entries which have a submission timestamp that is older than the specified number of
   * days.
   *
   * @param daysToRetain the number of days until which diagnosis keys will be retained.
   * @throws IllegalArgumentException if {@code daysToRetain} is negative.
   */
  @Timed
  @Transactional
  public void applyRetentionPolicy(final int daysToRetain) {
    final long threshold = daysToSeconds(daysToRetain);
    final int numberOfDeletions = keyRepository.countOlderThan(threshold);
    logger.info("Deleting {} diagnosis key(s) with a submission timestamp older than {} day(s) ago.", numberOfDeletions,
        daysToRetain);
    keyRepository.deleteOlderThan(threshold);
  }

  /**
   * Delete entries from 'self_report_submissions', which are older than given days.
   *
   * @param retentionDays - How many days should be kept in DB?
   */
  @Timed
  @Transactional
  public void applySrsRetentionPolicy(final int retentionDays) {
    final LocalDate retentionDate = now(UTC).minusDays(retentionDays);
    final int numberOfDeletions = keyRepository.countSrsOlderThan(retentionDate);
    logger.info("Deleting {} SRS with a submission date older than {} day(s) ago.", numberOfDeletions, retentionDays);
    keyRepository.deleteSrsOlderThan(retentionDate);
  }

  @Timed
  @Transactional
  public int countTodaysDiagnosisKeys() {
    final long midnightEpochHours = now(UTC).toEpochSecond(LocalTime.MIDNIGHT, UTC) / TimeUnit.HOURS.toSeconds(1);
    return keyRepository.countNewerThan(midnightEpochHours);
  }

  /**
   * Check if any of the key data is already stored in the DB, regardless of submission type.
   *
   * @param keys to be checked
   * @return <code>true</code> if one or more keys are already persisted.
   */
  @Timed
  @Transactional
  public boolean exists(final Collection<DiagnosisKey> keys) {
    if (ObjectUtils.isEmpty(keys)) {
      return false;
    }
    final Collection<byte[]> data = new ArrayList<>(keys.size());
    for (final DiagnosisKey key : keys) {
      data.add(key.getKeyData());
    }
    return keyRepository.exists(data);
  }

  /**
   * Returns all valid persisted diagnosis keys, sorted by their submission timestamp.
   *
   * @return ValidDiagnosisKeyFilter
   */
  @Timed
  @Transactional
  public Collection<DiagnosisKey> getDiagnosisKeys() {
    final Collection<DiagnosisKey> diagnosisKeys = createStreamFromIterator(
        keyRepository.findAll(Sort.by(Direction.ASC, "submissionTimestamp")).iterator()).toList();
    return validationFilter.filter(diagnosisKeys);
  }

  /**
   * Fetches {@link DiagnosisKey}s from DB with TRL greater or equal than the given value.
   *
   * @param minTrl      Minimum Transmission-Risk-Level to fetch.
   * @param daysToFetch time in days, that should be published
   * @return List of {@link DiagnosisKey}s filtered by {@link #validationFilter}.
   */
  @Timed
  @Transactional
  public Collection<DiagnosisKey> getDiagnosisKeysWithMinTrl(final int minTrl, final int daysToFetch) {
    final Collection<DiagnosisKey> diagnosisKeys = keyRepository.findAllWithTrlGreaterThanOrEqual(minTrl,
        daysToSeconds(daysToFetch));
    return validationFilter.filter(diagnosisKeys);
  }

  @Timed
  @Transactional
  public boolean recordSrs(final SubmissionType submissionType) {
    return keyRepository.recordSrs(submissionType.name());
  }

  /**
   * Persists the specified collection of {@link DiagnosisKey} instances and returns the number of inserted diagnosis
   * keys. If the key data of a particular diagnosis key already exists in the database and is of a submission type that
   * can not be overwritten with the new submission type (e.g. overwriting PCR with RAPID is not possible), this
   * diagnosis key is not persisted.
   *
   * @param diagnosisKeys must not contain {@literal null}.
   * @return Number of successfully inserted diagnosis keys.
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  @Timed
  @Transactional
  public int saveDiagnosisKeys(final Collection<DiagnosisKey> diagnosisKeys) {
    int numberOfInsertedKeys = 0;
    for (final DiagnosisKey diagnosisKey : diagnosisKeys) {
      if (keyRepository.exists(diagnosisKey.getKeyData(), SubmissionType.SUBMISSION_TYPE_PCR_TEST.name())) {
        continue;
      }
      final boolean keyInsertedSuccessfully = keyRepository.saveDoNothingOnConflict(
          diagnosisKey.getKeyData(), diagnosisKey.getRollingStartIntervalNumber(), diagnosisKey.getRollingPeriod(),
          diagnosisKey.getSubmissionTimestamp(), diagnosisKey.getTransmissionRiskLevel(),
          diagnosisKey.getOriginCountry(), diagnosisKey.getVisitedCountries().toArray(new String[0]),
          diagnosisKey.getReportType().name(), diagnosisKey.getDaysSinceOnsetOfSymptoms(),
          diagnosisKey.isConsentToFederation(), diagnosisKey.getSubmissionType().name());

      if (keyInsertedSuccessfully) {
        numberOfInsertedKeys++;
      }
    }
    final int conflictingKeys = diagnosisKeys.size() - numberOfInsertedKeys;
    if (conflictingKeys > 0) {
      logger.warn("{} out of {} diagnosis keys conflicted with existing database entries and were ignored.",
          conflictingKeys, diagnosisKeys.size());
    }
    return numberOfInsertedKeys;
  }
}
