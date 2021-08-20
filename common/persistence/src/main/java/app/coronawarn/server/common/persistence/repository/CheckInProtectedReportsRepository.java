package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.CheckInProtectedReports;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInProtectedReportsRepository extends PagingAndSortingRepository<CheckInProtectedReports, Long> {

  @Modifying
  @Query("INSERT INTO check_in_protected_reports (trace_location_id_hash, initialization_vector,"
      + " encrypted_check_in_record, mac, submission_timestamp)"
      + " VALUES (:trace_location_id_hash, :initialization_vector, :encrypted_check_in_record, "
      + " :mac, :submission_timestamp) ON CONFLICT DO NOTHING")
  boolean saveDoNothingOnConflict(
      @Param("trace_location_id_hash") byte[] traceLocationIdHash,
      @Param("initialization_vector") byte[] initializationVector,
      @Param("encrypted_check_in_record") byte[] encryptedCheckInRecord,
      @Param("mac") byte[] mac,
      @Param("submission_timestamp") Integer submissionTimestamp);

  /**
   * Counts all entries that have a submission timestamp older than the specified one.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be expired.
   * @return The number of expired submission timestamps.
   */
  @Query("SELECT COUNT(*) FROM check_in_protected_reports WHERE submission_timestamp<:threshold")
  int countOlderThan(@Param("threshold") long submissionTimestamp);

  /**
   * Deletes all entries that have a submission timestamp older than the specified one.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be deleted.
   */
  @Modifying
  @Query("DELETE FROM check_in_protected_reports WHERE submission_timestamp<:threshold")
  void deleteOlderThan(@Param("threshold") long submissionTimestamp);
}
