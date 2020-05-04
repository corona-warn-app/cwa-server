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
   * TODO doc explain
   * @param diagnosisKeys
   * @return
   */
  public Collection<DiagnosisKey> saveDiagnosisKeys(
      Collection<DiagnosisKey> diagnosisKeys) {
    return keyRepository.saveAll(diagnosisKeys);
  }
}
