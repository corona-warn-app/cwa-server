package app.coronawarn.server.services.distribution.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatisticsJsonStringObject {

  private String date;
  @JsonProperty("app_downloads_cum")
  private Integer appDownloadsCum;
  @JsonProperty("key_uploads")
  private Integer keyUploads;
  @JsonProperty("test_total")
  private Integer testTotal;
  @JsonProperty("total_redeemed")
  private Integer totalRedeemed;
  @JsonProperty("total_not_redeemed")
  private Integer totalNotRedeemed;
  @JsonProperty("labs_total")
  private Integer labsTotal;
  @JsonProperty("labs_done")
  private Integer labsDone;
  private Integer infections;
  @JsonProperty("day_of_week")
  private String dayOfWeek;
  @JsonProperty("week_of_year")
  private Integer weekOfYear;


  public String getDate() {
    return date;
  }

  public Integer getAppDownloadsCum() {
    return appDownloadsCum;
  }

  public Integer getKeyUploads() {
    return keyUploads;
  }

  public Integer getTestTotal() {
    return testTotal;
  }

  public Integer getTotalRedeemed() {
    return totalRedeemed;
  }

  public Integer getTotalNotRedeemed() {
    return totalNotRedeemed;
  }

  public Integer getLabsTotal() {
    return labsTotal;
  }

  public Integer getLabsDone() {
    return labsDone;
  }

  public Integer getInfections() {
    return infections;
  }

  public String getDayOfWeek() {
    return dayOfWeek;
  }

  public Integer getWeekOfYear() {
    return weekOfYear;
  }

}
