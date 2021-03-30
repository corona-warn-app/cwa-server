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
  @Query("INSERT INTO trace_time_interval_warning (trace_location_id, start_interval_number,"
      + " end_interval_number, transmission_risk_level)"
      + " VALUES (:trace_location_id, :start_interval_number, :end_interval_number, "
      + ":transmission_risk_level) ON CONFLICT DO NOTHING")
  boolean saveDoNothingOnConflict(@Param("trace_location_id") byte[] traceLocationGuid,
      @Param("start_interval_number") Integer startIntervalNumber,
      @Param("end_interval_number") Integer endIntervalNumber,
      @Param("transmission_risk_level") Integer transmissionRiskLevel);
}
