package app.coronawarn.server.common.persistence.service;

import app.coronawarn.server.common.persistence.domain.StatisticsDownload;
import app.coronawarn.server.common.persistence.repository.StatisticsDownloadRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StatisticsDownloadService {

  private StatisticsDownloadRepository repository;
  private static final Logger logger = LoggerFactory.getLogger(StatisticsDownloadService.class);

  public StatisticsDownloadService(StatisticsDownloadRepository repository) {
    this.repository = repository;
  }

  /**
   * Stores an entry in the Database with {@param timestamp} as seconds and {@param eTag}. The counter is an incremental
   * value automatically determined by a postgres sequence.
   * @param timestamp in seconds.
   * @param etag value to be stored.
   * @return boolean determining if store was successful or not.s
   */
  @Transactional
  public boolean store(long timestamp, String etag) {
    try {
      this.repository.insertWithAutoIncrement(timestamp, etag);
      return true;
    } catch (Exception e) {
      logger.error("Failed to store Statistics Download entry", e);
      return false;
    }
  }

  /**
   * Retrieves the latest {@link StatisticsDownload} stored. The order is determined by the counter field, which is
   * automatically incremented by the Database.
   * @return {@link StatisticsDownload} returns Optional.empty if no download entries are stored.
   */
  public Optional<StatisticsDownload> getMostRecentDownload() {
    var downloads = this.repository.getWithLatestETag();
    if (downloads.size() > 0) {
      return Optional.of(downloads.get(0));
    } else {
      return Optional.empty();
    }
  }
}
