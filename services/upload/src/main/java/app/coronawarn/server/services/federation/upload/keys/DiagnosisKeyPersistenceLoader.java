

package app.coronawarn.server.services.federation.upload.keys;

import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import app.coronawarn.server.common.persistence.service.common.ExpirationPolicy;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisKeyPersistenceLoader implements DiagnosisKeyLoader {

  private final FederationUploadKeyService uploadKeyService;
  private final UploadServiceConfig uploadConfig;
  private final EfgsKeyParameterAdapter efgsKeyAdapter;
  private final EfgsUploadKeyFilter efgsKeyFilter;

  DiagnosisKeyPersistenceLoader(FederationUploadKeyService uploadKeyService,
      UploadServiceConfig uploadConfig, EfgsKeyParameterAdapter efgsKeysAdapter,
      EfgsUploadKeyFilter efgsKeysFilter) {
    this.uploadKeyService = uploadKeyService;
    this.uploadConfig = uploadConfig;
    this.efgsKeyAdapter = efgsKeysAdapter;
    this.efgsKeyFilter = efgsKeysFilter;
  }

  /**
   * Retrieve all diagnosis keys which are ready for upload (replicated to the upload table),
   * filter out invalid keys and adapt parameters where applicable.
   */
  @Override
  public List<FederationUploadKey> loadDiagnosisKeys() {

    List<FederationUploadKey> readyForUploadKeys = this.uploadKeyService.getPendingUploadKeys(
        ExpirationPolicy.of(uploadConfig.getExpiryPolicyMinutes(), ChronoUnit.MINUTES),
        this.uploadConfig.getRetentionDays());
    List<FederationUploadKey> uploadableKeys = readyForUploadKeys.stream()
        .filter(efgsKeyFilter::isUploadable).collect(Collectors.toList());

    return efgsKeyAdapter.adaptToEfgsRequirements(uploadableKeys);
  }
}
