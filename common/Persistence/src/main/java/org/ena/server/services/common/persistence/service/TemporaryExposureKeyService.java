package org.ena.server.services.common.persistence.service;

import org.ena.server.services.common.persistence.repository.TemporaryExposureKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TemporaryExposureKeyService {

  @Autowired
  private TemporaryExposureKeyRepository keyRepository;

  //TODO add desired IO operations
}
