package app.coronawarn.server.services.common.persistence.service;

import java.util.Collection;
import app.coronawarn.server.services.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.common.persistence.repository.DiagnosisKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisKeyService {

  @Autowired
  private DiagnosisKeyRepository keyRepository;

  /**
   * Persists the specified collection of {@link DiagnosisKey} instances.
   *
   * @param diagnosisKeys must not contain {@literal null}.
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  public void saveDiagnosisKeys(Collection<DiagnosisKey> diagnosisKeys) {
    keyRepository.saveAll(diagnosisKeys);
  }
}
