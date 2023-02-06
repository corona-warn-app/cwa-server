package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FederationUploadKeyRepository extends CrudRepository<FederationUploadKey, Long> {

  Collection<FederationUploadKey> findAllUploadableKeys();

  void updateBatchTag(byte[] keyData, String batchTag);
}
