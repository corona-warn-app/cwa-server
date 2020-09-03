package app.coronawarn.server.services.federation.upload.keys;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!testdata")
public class DiagnosisKeyPersistenceLoader implements DiagnosisKeyLoader {

  private final FederationUploadKeyService uploadKeyService;

  public DiagnosisKeyPersistenceLoader(FederationUploadKeyService uploadKeyService) {
    this.uploadKeyService = uploadKeyService;
  }

  @Override
  public List<DiagnosisKey> loadDiagnosisKeys() {
    return this.uploadKeyService.getPendingUploadKeys();
  }

}
