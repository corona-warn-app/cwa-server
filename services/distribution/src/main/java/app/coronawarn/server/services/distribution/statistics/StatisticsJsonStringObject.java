package app.coronawarn.server.services.distribution.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatisticsJsonStringObject {

  @JsonProperty("update_timestamp")
  private String updateTimestamp;
  @JsonProperty("effective_date")
  private String effectiveDate;
  @JsonProperty("app_downloads_daily")
  private Integer appDownloadsDaily;
  @JsonProperty("app_downloads_cumulated")
  private Integer appDownloadsCumulated;
  @JsonProperty("app_downloads_7days_sum")
  private Integer appDownloads7daysSum;
  @JsonProperty("app_downloads_7days_growthrate")
  private Integer appDownloads7daysGrowthrate;
  @JsonProperty("app_downloads_7days_trend_5percent")
  private Integer appDownloads7daysTrend5percent;
  @JsonProperty("persons_who_shared_keys_daily")
  private Integer personsWhoSharedKeysDaily;
  @JsonProperty("persons_who_shared_keys_cumulated")
  private Integer personsWhoSharedKeysCumulated;
  @JsonProperty("persons_who_shared_keys_7days_sum")
  private Integer personsWhoSharedKeys7daysSum;
  @JsonProperty("persons_who_shared_keys_7days_growthrate")
  private Integer personsWhoSharedKeys7daysGrowthrate;
  @JsonProperty("persons_who_shared_keys_7days_trend_5percent")
  private Integer personsWhoSharedKeys7daysTrend5percent;
  @JsonProperty("tests_total_daily")
  private Integer testsTotalDaily;
  @JsonProperty("positive_tests_used_to_share_keys_daily")
  private Integer positiveTestsUsedToShareKeysDaily;
  @JsonProperty("positive_tests_not_used_to_share_keys_daily")
  private Integer positiveTestsNotUsedToShareKeysDaily;
  @JsonProperty("labs_registered_absolut")
  private Integer labsRegisteredAbsolut;
  @JsonProperty("labs_connected_absolut")
  private Integer labsConnectedAbsolut;
  @JsonProperty("seven_day_incidence")
  private Integer sevenDayIncidence;
  @JsonProperty("seven_day_incidence_growthrate")
  private Integer sevenDayIncidenceGrowthrate;
  @JsonProperty("seven_day_incidence_trend_5percent")
  private Integer sevenDayIncidenceTrend5percent;
  @JsonProperty("infections_reported_daily")
  private Integer infectionsReportedDaily;
  @JsonProperty("infections_reported_7days_avg")
  private Integer infectionsReported7daysAvg;
  @JsonProperty("infections_reported_7days_growthrate")
  private Integer infectionsReported7daysGrowthrate;
  @JsonProperty("infections_reported_7days_trend_5percent")
  private Integer infectionsReported7daysTrend5percent;
  @JsonProperty("infections_reported_cumulated")
  private Integer infectionsReportedCumulated;

  public String getUpdateTimestamp() {
    return updateTimestamp;
  }

  public String getEffectiveDate() {
    return effectiveDate;
  }

  public Integer getAppDownloadsDaily() {
    return appDownloadsDaily;
  }

  public Integer getAppDownloadsCumulated() {
    return appDownloadsCumulated;
  }

  public Integer getAppDownloads7daysSum() {
    return appDownloads7daysSum;
  }

  public Integer getAppDownloads7daysGrowthrate() {
    return appDownloads7daysGrowthrate;
  }

  public Integer getAppDownloads7daysTrend5percent() {
    return appDownloads7daysTrend5percent;
  }

  public Integer getPersonsWhoSharedKeysDaily() {
    return personsWhoSharedKeysDaily;
  }

  public Integer getPersonsWhoSharedKeysCumulated() {
    return personsWhoSharedKeysCumulated;
  }

  public Integer getPersonsWhoSharedKeys7daysSum() {
    return personsWhoSharedKeys7daysSum;
  }

  public Integer getPersonsWhoSharedKeys7daysGrowthrate() {
    return personsWhoSharedKeys7daysGrowthrate;
  }

  public Integer getPersonsWhoSharedKeys7daysTrend5percent() {
    return personsWhoSharedKeys7daysTrend5percent;
  }

  public Integer getTestsTotalDaily() {
    return testsTotalDaily;
  }

  public Integer getPositiveTestsUsedToShareKeysDaily() {
    return positiveTestsUsedToShareKeysDaily;
  }

  public Integer getPositiveTestsNotUsedToShareKeysDaily() {
    return positiveTestsNotUsedToShareKeysDaily;
  }

  public Integer getLabsRegisteredAbsolut() {
    return labsRegisteredAbsolut;
  }

  public Integer getLabsConnectedAbsolut() {
    return labsConnectedAbsolut;
  }

  public Integer getSevenDayIncidence() {
    return sevenDayIncidence;
  }

  public Integer getSevenDayIncidenceGrowthrate() {
    return sevenDayIncidenceGrowthrate;
  }

  public Integer getSevenDayIncidenceTrend5percent() {
    return sevenDayIncidenceTrend5percent;
  }

  public Integer getInfectionsReportedDaily() {
    return infectionsReportedDaily;
  }

  public Integer getInfectionsReported7daysAvg() {
    return infectionsReported7daysAvg;
  }

  public Integer getInfectionsReported7daysGrowthrate() {
    return infectionsReported7daysGrowthrate;
  }

  public Integer getInfectionsReported7daysTrend5percent() {
    return infectionsReported7daysTrend5percent;
  }

  public Integer getInfectionsReportedCumulated() {
    return infectionsReportedCumulated;
  }
}
