/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.common.persistence.service;

import static app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestampValidator.SECONDS_PER_HOUR;
import static java.time.ZoneOffset.UTC;
import static org.springframework.data.util.StreamUtils.createStreamFromIterator;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DiagnosisKeyService {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyService.class);
  private final DiagnosisKeyRepository keyRepository;

  public DiagnosisKeyService(DiagnosisKeyRepository keyRepository) {
    this.keyRepository = keyRepository;
  }

  /**
   * Persists the specified collection of {@link DiagnosisKey} instances. If the key data of a particular diagnosis key
   * already exists in the database, this diagnosis key is not persisted.
   *
   * @param diagnosisKeys must not contain {@literal null}.
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  @Timed
  @Transactional
  public void saveDiagnosisKeys(Collection<DiagnosisKey> diagnosisKeys) {
    for (DiagnosisKey diagnosisKey : diagnosisKeys) {
      keyRepository.saveDoNothingOnConflict(
          diagnosisKey.getKeyData(), diagnosisKey.getRollingStartIntervalNumber(), diagnosisKey.getRollingPeriod(),
          diagnosisKey.getSubmissionTimestamp(), diagnosisKey.getTransmissionRiskLevel(),
          diagnosisKey.getOriginCountry(), diagnosisKey.getVisitedCountries().toArray(new String[0]));
    }
  }

  /**
   * Returns all valid persisted diagnosis keys, sorted by their submission timestamp.
   */
  public List<DiagnosisKey> getDiagnosisKeys() {
    List<DiagnosisKey> diagnosisKeys = createStreamFromIterator(
        keyRepository.findAll(Sort.by(Direction.ASC, "submissionTimestamp")).iterator()).collect(Collectors.toList());
    return this.filterValidDiagnosisKeys(diagnosisKeys);
  }

  /**
   * Return all valid persisted diagnosis keys, sorted by their submission timestamp where visited_countries
   * contains {@param countryCode}.
   *
   * @param countryCode country filter.
   * @return Collection of {@link DiagnosisKey} that have visited_country in their array.
   */
  public List<DiagnosisKey> getDiagnosisKeysByVisitedCountry(String countryCode) {
    var diagnosisKeys = createStreamFromIterator(
        keyRepository.findAllKeysWhereVisitedCountryContains(countryCode).iterator()).collect(Collectors.toList());
    return this.filterValidDiagnosisKeys(diagnosisKeys);
  }

  private List<DiagnosisKey> filterValidDiagnosisKeys(List<DiagnosisKey> diagnosisKeys) {
    List<DiagnosisKey> validDiagnosisKeys =
        diagnosisKeys.stream().filter(DiagnosisKeyService::isDiagnosisKeyValid).collect(Collectors.toList());

    int numberOfDiscardedKeys = diagnosisKeys.size() - validDiagnosisKeys.size();
    logger.info("Retrieved {} diagnosis key(s). Discarded {} diagnosis key(s) from the result as invalid.",
        diagnosisKeys.size(), numberOfDiscardedKeys);

    return validDiagnosisKeys;
  }

  private static boolean isDiagnosisKeyValid(DiagnosisKey diagnosisKey) {
    Collection<ConstraintViolation<DiagnosisKey>> violations = diagnosisKey.validate();
    boolean isValid = violations.isEmpty();

    if (!isValid) {
      List<String> violationMessages =
          violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
      logger.warn("Validation failed for diagnosis key from database. Violations: {}", violationMessages);
    }

    return isValid;
  }

  /**
   * Deletes all diagnosis key entries which have a submission timestamp that is older than the specified number of
   * days.
   *
   * @param daysToRetain the number of days until which diagnosis keys will be retained.
   * @param countryCode country filter.
   * @throws IllegalArgumentException if {@code daysToRetain} is negative.
   */
  @Transactional
  public void applyRetentionPolicy(int daysToRetain, String countryCode) {
    if (daysToRetain < 0) {
      throw new IllegalArgumentException("Number of days to retain must be greater or equal to 0.");
    }

    long threshold = LocalDateTime
        .ofInstant(Instant.now(), UTC)
        .minusDays(daysToRetain)
        .toEpochSecond(UTC) / SECONDS_PER_HOUR;
    int numberOfDeletions = keyRepository.countOlderThanOrEqual(threshold, countryCode);
    logger.info("[{}] Deleting {} diagnosis key(s) with a submission timestamp older than {} day(s) ago.",
        countryCode, numberOfDeletions, daysToRetain);
    keyRepository.deleteOlderThanOrEqual(threshold, countryCode);
  }
}
