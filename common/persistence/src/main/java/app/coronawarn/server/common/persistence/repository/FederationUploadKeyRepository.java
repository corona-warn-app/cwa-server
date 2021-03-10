package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import java.util.List;

public interface FederationUploadKeyRepository {

  List<FederationUploadKey> findAllUploadableKeys();

  Iterable<FederationUploadKey> findAll();

  void updateBatchTag(byte[] keyData, String batchTag);
}
