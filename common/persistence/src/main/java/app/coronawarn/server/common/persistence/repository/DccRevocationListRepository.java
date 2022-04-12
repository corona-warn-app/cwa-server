package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import java.util.Collection;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DccRevocationListRepository extends PagingAndSortingRepository<RevocationEntry, Long> {

  @Query("SELECT id, kid || type AS kid, type, hash, xhash, yhash  FROM revocation_entry")
  public Collection<RevocationEntry> getHashWithKidAndTypeConnected();

}
