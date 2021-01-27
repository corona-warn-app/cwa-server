package app.coronawarn.server.common.persistence.service;

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.StatisticsDownload;
import app.coronawarn.server.common.persistence.repository.StatisticsDownloadRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StatisticsDownloadService {

  private final StatisticsDownloadRepository repository;
  private static final Logger logger = LoggerFactory.getLogger(StatisticsDownloadService.class);

  public StatisticsDownloadService(StatisticsDownloadRepository repository) {
    this.repository = repository;
  }

  /**
   * Stores an entry in the Database with {@param timestamp} as seconds and {@param eTag}. The counter is an incremental
   * value automatically determined by a postgres sequence.
   *
   * @param timestamp in seconds.
   * @param etag      value to be stored.
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
   *
   * @return {@link StatisticsDownload} returns Optional.empty if no download entries are stored.
   */
  public Optional<StatisticsDownload> getMostRecentDownload() {
    return Optional.ofNullable(repository.getWithLatestETag());
  }

  /**
   * Delete all downloaded entries for JSON statistics older than `today - {@param retentionDays}`.
   *
   * @param retentionDays number of days to retain the data.
   */
  public void applyRetentionPolicy(Integer retentionDays) {
    if (retentionDays < 0) {
      throw new IllegalArgumentException("Number of days to retain must be greater or equal to 0.");
    }
    long threshold = LocalDateTime
        .ofInstant(Instant.now(), UTC)
        .minusDays(retentionDays)
        .toEpochSecond(UTC);
    int numberOfDeletions = this.repository.countDownloadEntriesOlderThan(threshold);
    logger.info("Deleting {} Statistics JSON record(s) with a downloaded timestamp older than {} day(s) ago.",
        numberOfDeletions, retentionDays);
    this.repository.deleteDownloadEntriesOlderThan(threshold);
  }
}
