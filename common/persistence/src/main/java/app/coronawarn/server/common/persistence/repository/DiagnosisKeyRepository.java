package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosisKeyRepository extends JpaRepository<DiagnosisKey, Long> {

  /**
   * Deletes all entries that have a submission timestamp lesser or equal to the specified one.
   *
   * @param submissionTimestamp the submission timestamp up to which entries will be deleted.
   */
  @Transactional
  public void deleteBySubmissionTimestampIsLessThanEqual(long submissionTimestamp);
}
