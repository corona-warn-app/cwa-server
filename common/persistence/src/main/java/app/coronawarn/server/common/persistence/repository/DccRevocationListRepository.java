package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DccRevocationListRepository extends PagingAndSortingRepository<RevocationEntry, Long> {

}
