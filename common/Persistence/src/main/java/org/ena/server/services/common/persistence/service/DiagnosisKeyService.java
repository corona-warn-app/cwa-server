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
   * @param diagnosisKey
   * @return
   */
  public Collection<DiagnosisKey> saveDiagnosisKey(
      Collection<DiagnosisKey> diagnosisKey) {
    return keyRepository.saveAll(diagnosisKey);
  }
}
