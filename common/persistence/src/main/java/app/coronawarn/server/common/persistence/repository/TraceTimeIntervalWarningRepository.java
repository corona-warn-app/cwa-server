package app.coronawarn.server.common.persistence.repository;


import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Event check-in repository.
 * @deprecated in favor of encrypted check-ins.
 */
@Deprecated(since = "2.8", forRemoval = true)
@Repository
public interface TraceTimeIntervalWarningRepository
    extends PagingAndSortingRepository<TraceTimeIntervalWarning, Long> {

  @Modifying
  @Query("INSERT INTO trace_time_interval_warning (trace_location_id, start_interval_number,"
      + " period, transmission_risk_level, submission_timestamp, submission_type)"
      + " VALUES (:trace_location_id, :start_interval_number, :period, "
      + ":transmission_risk_level, :submission_timestamp, :submission_type) ON CONFLICT DO NOTHING")
  boolean saveDoNothingOnConflict(@Param("trace_location_id") byte[] traceLocationGuid,
      @Param("start_interval_number") Integer startIntervalNumber, @Param("period") Integer period,
      @Param("transmission_risk_level") Integer transmissionRiskLevel,
      @Param("submission_timestamp") Integer submissionTimestamp,
      @Param("submission_type") String submissionType);

  /**
   * Counts all entries that have a submission timestamp older than the specified one.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be expired.
   * @return The number of expired trace time warnings.
   */
  @Query("SELECT COUNT(*) FROM trace_time_interval_warning WHERE submission_timestamp<:threshold")
  int countOlderThan(@Param("threshold") long submissionTimestamp);

  /**
   * Deletes all entries that have a submission timestamp older than the specified one.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be deleted.
   */
  @Modifying
  @Query("DELETE FROM trace_time_interval_warning WHERE submission_timestamp<:threshold")
  void deleteOlderThan(@Param("threshold") long submissionTimestamp);
}
