package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Provides db operations for diagnosis keys which are uploadable to the Swiss Gateway.
 */
@Repository
@Profile("connect-chgs")
public interface ChgsUploadKeyRepository extends FederationUploadKeyRepository {

  @Query("SELECT * FROM chgs_upload_key WHERE (batch_tag is null or batch_tag = '')")
  List<FederationUploadKey> findAllUploadableKeys();

  @Query("SELECT * FROM chgs_upload_key")
  List<FederationUploadKey> findAll();

  @Modifying
  @Query("update chgs_upload_key set batch_tag = :batchTag where key_data = :keyData")
  void updateBatchTag(@Param("keyData") byte[] keyData, @Param("batchTag") String batchTag);
}
