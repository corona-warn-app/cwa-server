package app.coronawarn.server.common.persistence.service;

import app.coronawarn.server.common.persistence.domain.StatisticsDownload;
import app.coronawarn.server.common.persistence.repository.StatisticsDownloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Component
public class StatisticsDownloadService {

  private StatisticsDownloadRepository repository;
  private static final Logger logger = LoggerFactory.getLogger(StatisticsDownloadService.class);

  public StatisticsDownloadService(StatisticsDownloadRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public boolean save(StatisticsDownload download) {
    try {
      this.repository.save(download);
      return true;
    } catch (Exception e) {
      logger.error("Failed to store Statistics Download entry", e);
      return false;
    }
  }

  @Transactional
  public boolean store(long timestamp, String eTag) {
    try {
      this.repository.insertWithAutoIncrement(timestamp, eTag);
      return true;
    } catch (Exception e) {
      logger.error("Failed to store Statistics Download entry", e);
      return false;
    }
  }

  public Optional<StatisticsDownload> getMostRecentDownload() {
    var downloads = this.repository.getWithLatestETag();
    if (downloads.size() > 0) {
      return Optional.of(downloads.get(0));
    } else {
      return Optional.empty();
    }
//    this.repository.getWithLatestETag().get(0);
//    return Optional.ofNullable(this.repository.getWithLatestETag().get(0));
  }
}
