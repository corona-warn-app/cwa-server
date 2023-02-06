package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.common.persistence.domain.RevocationEntryId;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DccRevocationListRepository extends CrudRepository<RevocationEntry, RevocationEntryId> {

  @Modifying
  @Query("INSERT INTO revocation_entry (kid, type, hash) VALUES (:kid, :type, :hash) ON CONFLICT DO NOTHING")
  void saveDoNothingOnConflict(@Param("kid") byte[] kid, @Param("type") byte[] type, @Param("hash") byte[] hash);

  @Modifying
  @Query("TRUNCATE revocation_entry")
  void truncate();
}
