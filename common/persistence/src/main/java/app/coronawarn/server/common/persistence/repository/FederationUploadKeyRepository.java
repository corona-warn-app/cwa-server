

package app.coronawarn.server.common.persistence.repository;


import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FederationUploadKeyRepository
    extends PagingAndSortingRepository<FederationUploadKey, Long> {

  @Query("SELECT * FROM federation_upload_key WHERE (batch_tag is null or batch_tag = '')")
  List<FederationUploadKey> findAllUploadableKeys();

  @Modifying
  @Query("update federation_upload_key set batch_tag = :batchTag where key_data = :keyData")
  void updateBatchTag(@Param("keyData") byte[] keyData, @Param("batchTag") String batchTag);
}
