package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.StatisticsDownload;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticsDownloadRepository extends CrudRepository<StatisticsDownload, Integer> {

  @Query("SELECT * FROM statistics_downloaded ORDER BY counter DESC")
  List<StatisticsDownload> getWithLatestETag();

  @Modifying
  @Query("INSERT INTO statistics_downloaded (downloaded_timestamp, etag) VALUES (:timestamp, :etag)")
  void insertWithAutoIncrement(@Param("timestamp") long timestamp, @Param("etag") String etag);

}
