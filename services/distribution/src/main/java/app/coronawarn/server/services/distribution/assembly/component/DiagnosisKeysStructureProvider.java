package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.DiagnosisKeysDirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Retrieves stored diagnosis keys and builds a {@link DiagnosisKeysDirectoryImpl} with them.
 */
@Component
public class DiagnosisKeysStructureProvider {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeysStructureProvider.class);

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private CryptoProvider cryptoProvider;

  public Directory getDiagnosisKeys() {
    Collection<DiagnosisKey> diagnosisKeys = readDiagnosisKeys();
    return new DiagnosisKeysDirectoryImpl(diagnosisKeys, cryptoProvider);
  }

  private Collection<DiagnosisKey> readDiagnosisKeys() {
    logger.debug("Querying diagnosis keys from the database...");
    return diagnosisKeyService.getDiagnosisKeys();
  }

}
