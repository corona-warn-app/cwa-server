package app.coronawarn.server.common.persistence.repository;


import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceTimeIntervalWarningRepository
    extends CrudRepository<TraceTimeIntervalWarning, Long> {

  @Modifying
  @Query("INSERT INTO trace_time_interval_warning (trace_location_guid, start_interval_number,"
      + " period, transmission_risk_level, submission_timestamp)"
      + " VALUES (:trace_location_guid, :start_interval_number, :period, "
      + ":transmission_risk_level, :submission_timestamp) ON CONFLICT DO NOTHING")
  boolean saveDoNothingOnConflict(@Param("trace_location_guid") byte[] traceLocationGuid,
      @Param("start_interval_number") Integer startIntervalNumber,
      @Param("period") Integer period,
      @Param("transmission_risk_level") Integer transmissionRiskLevel,
      @Param("submission_timestamp") Integer submissionTimestamp);
}
