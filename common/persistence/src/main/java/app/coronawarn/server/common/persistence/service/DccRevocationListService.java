package app.coronawarn.server.common.persistence.service;

import static org.springframework.data.util.StreamUtils.createStreamFromIterator;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.common.persistence.repository.DccRevocationListRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DccRevocationListService {

  private static final Logger logger = LoggerFactory.getLogger(DccRevocationListService.class);
  private final DccRevocationListRepository dccRevocationListRepository;

  public DccRevocationListService(DccRevocationListRepository dccRevocationListRepository) {
    this.dccRevocationListRepository = dccRevocationListRepository;
  }

  /**
   * Store the DCC Revocation List entries.
   * @param revocationEntries list with parsed entries from DCC chuck.lst
   */
  public void store(Collection<RevocationEntry> revocationEntries) {
    logger.info("Saving Revocation list entries...");
    dccRevocationListRepository.saveAll(revocationEntries);
    logger.info("Revocation list entries has been saved!");
  }

  /**
   * Retrieves DCC Revocation List entries.
   * @return list of DCCRevocationEntries
   */
  public List<RevocationEntry> getRevocationListEntries() {
    List<RevocationEntry> revocationEntries =
        createStreamFromIterator(dccRevocationListRepository.findAll().iterator()).collect(Collectors.toList());
    return revocationEntries;
  }

  public Collection<RevocationEntry> getRevocationListByKidAndHash() {
    return dccRevocationListRepository.getHashWithKidAndTypeConnected();
  }
}
