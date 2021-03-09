package app.coronawarn.server.common.persistence.eventregistration.repository;

import app.coronawarn.server.common.persistence.eventregistration.domain.TraceTimeIntervalWarning;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceTimeIntervalWarningRepository extends CrudRepository<TraceTimeIntervalWarning, Long> {

}
