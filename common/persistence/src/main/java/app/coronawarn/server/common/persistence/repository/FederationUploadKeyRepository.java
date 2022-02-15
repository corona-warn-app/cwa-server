package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import java.util.Collection;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface FederationUploadKeyRepository extends PagingAndSortingRepository<FederationUploadKey, Long> {

  Collection<FederationUploadKey> findAllUploadableKeys();

  void updateBatchTag(byte[] keyData, String batchTag);
}
