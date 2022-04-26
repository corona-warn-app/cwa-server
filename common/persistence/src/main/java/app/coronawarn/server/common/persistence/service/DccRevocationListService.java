package app.coronawarn.server.common.persistence.service;

import static java.util.stream.StreamSupport.stream;
import static org.springframework.data.util.StreamUtils.createStreamFromIterator;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.common.persistence.domain.RevocationEtag;
import app.coronawarn.server.common.persistence.repository.DccRevocationEtagRepository;
import app.coronawarn.server.common.persistence.repository.DccRevocationListRepository;
import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DccRevocationListService {

  private static final Logger logger = LoggerFactory.getLogger(DccRevocationListService.class);

  private final DccRevocationListRepository repository;

  private final DccRevocationEtagRepository etagRepository;

  public DccRevocationListService(final DccRevocationListRepository repository,
      final DccRevocationEtagRepository etagRepository) {
    this.repository = repository;
    this.etagRepository = etagRepository;
  }

  /**
   * Check if given ETag already exists in DB.
   *
   * @param etag to be checked
   * @return <code>true</code> if and only if the ETag is stored in DB, <code>false</code> otherwise
   */
  public boolean etagExists(final String etag) {
    if (etag == null) {
      return false;
    }
    return stream(etagRepository.findAll().spliterator(), false).anyMatch(e -> etag.equals(e.getEtag()));
  }

  /**
   * Retrieves DCC Revocation List entries.
   *
   * @return list of DCCRevocationEntries
   */
  public Collection<RevocationEntry> getRevocationListEntries() {
    return createStreamFromIterator(repository.findAll().iterator()).collect(Collectors.toList());
  }

  /**
   * Store the DCC Revocation List entries.
   *
   * @param revocationEntries list with parsed entries from DCC chuck.lst
   */
  @Timed
  @Transactional
  public void store(final Collection<RevocationEntry> revocationEntries) {
    logger.info("Saving Revocation list entries...");
    for (final RevocationEntry entry : revocationEntries) {
      repository.saveDoNothingOnConflict(entry.getKid(), entry.getType(), entry.getHash());
    }
    logger.info("{} Revocation list entries saved!", revocationEntries.size());
  }

  /**
   * Stores given ETag.
   *
   * @param etag to be stored
   */
  @Transactional
  public void store(final RevocationEtag etag) {
    if (etagRepository.existsById(etag.getPath())) {
      etagRepository.deleteById(etag.getPath());
    }
    etagRepository.save(etag.getPath(), etag.getEtag());
  }

  public void truncate() {
    repository.truncate();
  }
}
