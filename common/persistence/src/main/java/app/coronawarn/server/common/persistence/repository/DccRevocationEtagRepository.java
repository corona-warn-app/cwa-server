package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.RevocationEtag;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DccRevocationEtagRepository extends PagingAndSortingRepository<RevocationEtag, String> {

  @Modifying
  @Query("INSERT INTO revocation_etag (path, etag) VALUES (:path, :etag)")
  void save(@Param("path") String path, @Param("etag") String etag);
}
