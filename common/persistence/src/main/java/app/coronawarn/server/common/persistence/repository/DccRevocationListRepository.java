package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.DccRevocationEntry;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DccRevocationListRepository extends PagingAndSortingRepository<DccRevocationEntry, Long> {

}
