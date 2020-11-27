

package app.coronawarn.server.services.federation.upload.keys;

import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import app.coronawarn.server.common.persistence.service.common.ExpirationPolicy;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisKeyPersistenceLoader implements DiagnosisKeyLoader {

  private final FederationUploadKeyService uploadKeyService;
  private final UploadServiceConfig uploadConfig;
  private final EfgsKeyParameterAdapter efgsKeyAdapter;

  public DiagnosisKeyPersistenceLoader(FederationUploadKeyService uploadKeyService,
      UploadServiceConfig uploadConfig, EfgsKeyParameterAdapter efgsKeysFilter) {
    this.uploadKeyService = uploadKeyService;
    this.uploadConfig = uploadConfig;
    this.efgsKeyAdapter = efgsKeysFilter;
  }

  @Override
  public List<FederationUploadKey> loadDiagnosisKeys() {
    return efgsKeyAdapter.adaptToEfgsRequirements(this.uploadKeyService
        .getPendingUploadKeys(
            ExpirationPolicy.of(uploadConfig.getExpiryPolicyMinutes(), ChronoUnit.MINUTES),
            this.uploadConfig.getRetentionDays()));
  }
}
