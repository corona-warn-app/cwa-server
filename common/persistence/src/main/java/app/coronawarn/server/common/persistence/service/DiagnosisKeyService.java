/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.common.persistence.service;

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisKeyService {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyService.class);
  private final DiagnosisKeyRepository keyRepository;

  @Autowired
  public DiagnosisKeyService(DiagnosisKeyRepository keyRepository) {
    this.keyRepository = keyRepository;
  }

  /**
   * Persists the specified collection of {@link DiagnosisKey} instances.
   *
   * @param diagnosisKeys must not contain {@literal null}.
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  public void saveDiagnosisKeys(Collection<DiagnosisKey> diagnosisKeys) {
    keyRepository.saveAll(diagnosisKeys);
  }

  /**
   * Returns all valid persisted diagnosis keys, sorted by their submission timestamp.
   */
  public List<DiagnosisKey> getDiagnosisKeys() {
    return keyRepository.findAll(Sort.by(Direction.ASC, "submissionTimestamp")).stream()
        .filter(DiagnosisKeyService::isDiagnosisKeyValid).collect(Collectors.toList());
  }

  private static boolean isDiagnosisKeyValid(DiagnosisKey diagnosisKey) {
    Collection<ConstraintViolation<DiagnosisKey>> violations = diagnosisKey.validate();
    boolean isValid = violations.isEmpty();

    if (!isValid) {
      logger.warn("Validation failed for diagnosis key from database. Diagnosis key: {}, Violations: {}",
          diagnosisKey, violations);
    }

    return isValid;
  }

  /**
   * Deletes all diagnosis key entries which have a submission timestamp that is older than the specified number of
   * days.
   *
   * @param daysToRetain the number of days until which diagnosis keys will be retained.
   * @throws IllegalArgumentException if {@code daysToRetain} is negative.
   */
  public void applyRetentionPolicy(int daysToRetain) {
    if (daysToRetain < 0) {
      throw new IllegalArgumentException("Number of days to retain must be greater or equal to 0.");
    }

    long threshold = LocalDateTime
        .ofInstant(Instant.now(), UTC)
        .minusDays(daysToRetain)
        .toEpochSecond(UTC) / 3600L;
    keyRepository.deleteBySubmissionTimestampIsLessThanEqual(threshold);
  }
}
