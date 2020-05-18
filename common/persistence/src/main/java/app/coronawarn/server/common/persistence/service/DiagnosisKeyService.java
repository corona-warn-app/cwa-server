package app.coronawarn.server.common.persistence.service;

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisKeyService {

  @Autowired
  private DiagnosisKeyRepository keyRepository;

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
   * Returns all persisted diagnosis keys, sorted by their submission timestamp.
   */
  public List<DiagnosisKey> getDiagnosisKeys() {
    return keyRepository.findAll(Sort.by(Sort.Direction.ASC, "submissionTimestamp"));
  }

  /**
   * Deletes all diagnosis key entries which have a submission timestamp that is older than the specified number of
   * days.
   *
   * @param daysToRetain the number of days until which diagnosis keys will be retained.
   */
  public void applyRetentionPolicy(int daysToRetain) {
    long threshold = LocalDateTime
        .ofInstant(Instant.now(), UTC)
        .minusDays(daysToRetain)
        .toEpochSecond(UTC) / 3600L;
    keyRepository.deleteBySubmissionTimestampIsLessThanEqual(threshold);
  }
}
