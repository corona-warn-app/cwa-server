package app.coronawarn.server.services.eventregistration.repository;


import app.coronawarn.server.services.eventregistration.domain.TraceLocation;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraceLocationRepository extends CrudRepository<TraceLocation, Long> {

  @Modifying
  @Query("INSERT INTO trace_location (trace_location_guid_hash, version, created_at)"
      + " VALUES (:traceLocationGuidHash,:version,:createdAt)")
  void save(@Param("traceLocationGuidHash") byte[] traceLocationGuidHash,
      @Param("version") Integer version,
      @Param("createdAt") Long createdAt);

  @Query("SELECT * FROM trace_location AS tl WHERE tl.trace_location_guid_hash=:traceLocationGuidHash")
  Optional<TraceLocation> findTraceLocationByGuidHash(
      @Param("traceLocationGuidHash") byte[] traceLocationGuidHash);
}
