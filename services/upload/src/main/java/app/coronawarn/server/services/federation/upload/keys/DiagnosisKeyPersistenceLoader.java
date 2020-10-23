

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

  public DiagnosisKeyPersistenceLoader(FederationUploadKeyService uploadKeyService,
      UploadServiceConfig uploadConfig) {
    this.uploadKeyService = uploadKeyService;
    this.uploadConfig = uploadConfig;
  }

  @Override
  public List<FederationUploadKey> loadDiagnosisKeys() {
    return this.uploadKeyService
        .getPendingUploadKeys(
            ExpirationPolicy.of(uploadConfig.getExpiryPolicyMinutes(), ChronoUnit.MINUTES),
            this.uploadConfig.getRetentionDays());
  }
}
