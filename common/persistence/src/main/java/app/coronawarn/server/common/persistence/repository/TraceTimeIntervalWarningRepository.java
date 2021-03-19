package app.coronawarn.server.common.persistence.repository;


import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceTimeIntervalWarningRepository extends CrudRepository<TraceTimeIntervalWarning, Long> {

}
