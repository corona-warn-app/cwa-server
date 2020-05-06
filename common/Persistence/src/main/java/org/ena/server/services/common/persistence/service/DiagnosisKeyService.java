package org.ena.server.services.common.persistence.service;

import java.util.Collection;
import org.ena.server.services.common.persistence.domain.DiagnosisKey;
import org.ena.server.services.common.persistence.repository.DiagnosisKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisKeyService {

  @Autowired
  private DiagnosisKeyRepository keyRepository;

  /**
   * Persists the specified collection of {@link DiagnosisKey} instances. Use the returned
   * collection for further operations as the saveDiagnosisKeys operation might have changed the
   * {@link DiagnosisKey} instances completely.
   *
   * @param diagnosisKeys must not contain {@literal null}.
   * @return a collection of the saved keys; will never contain {@literal null}.
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  public Collection<DiagnosisKey> saveDiagnosisKeys(
      Collection<DiagnosisKey> diagnosisKeys) {
    return keyRepository.saveAll(diagnosisKeys);
  }
}
