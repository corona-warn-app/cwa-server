package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.StatisticsDownloaded;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticsDownloadRepository extends CrudRepository<StatisticsDownloaded, Integer> {

  @Query("SELECT * FROM statistics_downloaded ORDER BY counter DESC fetch first 1 rows only")
  StatisticsDownloaded getWithLatestETag();

  @Modifying
  @Query("INSERT INTO statistics_downloaded (downloaded_timestamp, etag) VALUES (:timestamp, :etag)")
  void insertWithAutoIncrement(@Param("timestamp") long timestamp, @Param("etag") String etag);

  @Modifying
  @Query("DELETE FROM statistics_downloaded WHERE downloaded_timestamp <= :cutoff")
  void deleteDownloadEntriesOlderThan(@Param("cutoff") long timestamp);

  @Query("SELECT COUNT(*) FROM statistics_downloaded WHERE downloaded_timestamp <= :cutoff")
  int countDownloadEntriesOlderThan(@Param("cutoff") long timestamp);

}
