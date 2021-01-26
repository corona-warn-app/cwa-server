package app.coronawarn.server.common.persistence.domain;

import org.springframework.data.annotation.Id;

public class StatisticsDownload {

  @Id
  private final Integer counter;

  private final long downloadedTimestamp;

  private final String etag;

  public StatisticsDownload(Integer counter, long downloadedTimestamp, String etag) {
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
