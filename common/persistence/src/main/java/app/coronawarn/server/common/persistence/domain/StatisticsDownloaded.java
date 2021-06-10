package app.coronawarn.server.common.persistence.domain;

import org.springframework.data.annotation.Id;

public class StatisticsDownloaded {

  @Id
  private final Integer counter;

  private final long downloadedTimestamp;

  private final String etag;

  /**
   * Statistic JSON Download record POJO.
   * @param counter file counter/version. Incremented via a postgres sequence.
   * @param downloadedTimestamp timestamp in seconds when this record was downloaded.
   * @param etag etag value of the JSON file at the downloaded time.
   */
  public StatisticsDownloaded(Integer counter, long downloadedTimestamp, String etag) {
    this.counter = counter;
    this.downloadedTimestamp = downloadedTimestamp;
    this.etag = etag;
  }

  public Integer getCounter() {
    return counter;
  }

  public long getDownloadedTimestamp() {
    return downloadedTimestamp;
  }

  public String getEtag() {
    return etag;
  }
}
