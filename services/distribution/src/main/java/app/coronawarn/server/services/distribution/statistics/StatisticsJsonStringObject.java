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
  private Double sevenDayIncidenceGrowthrate;
  @JsonProperty("seven_day_incidence_trend_5percent")
  private Integer sevenDayIncidenceTrend5percent;
  @JsonProperty("infections_reported_daily")
  private Integer infectionsReportedDaily;
  @JsonProperty("infections_reported_7days_avg")
  private Integer infectionsReported7daysAvg;
  @JsonProperty("infections_reported_7days_growthrate")
  private Double infectionsReported7daysGrowthrate;
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

  public Integer getSevenDayIncidenceTrend5percent() {
    return sevenDayIncidenceTrend5percent;
  }

  public Integer getInfectionsReportedDaily() {
    return infectionsReportedDaily;
  }

  public Integer getInfectionsReported7daysAvg() {
    return infectionsReported7daysAvg;
  }


  public Integer getInfectionsReported7daysTrend5percent() {
    return infectionsReported7daysTrend5percent;
  }

  public Integer getInfectionsReportedCumulated() {
    return infectionsReportedCumulated;
  }

  public void setUpdateTimestamp(String updateTimestamp) {
    this.updateTimestamp = updateTimestamp;
  }

  public Double getSevenDayIncidenceGrowthrate() {
    return sevenDayIncidenceGrowthrate;
  }

  public void setSevenDayIncidenceGrowthrate(Double sevenDayIncidenceGrowthrate) {
    this.sevenDayIncidenceGrowthrate = sevenDayIncidenceGrowthrate;
  }

  public Double getInfectionsReported7daysGrowthrate() {
    return infectionsReported7daysGrowthrate;
  }

  public void setInfectionsReported7daysGrowthrate(Double infectionsReported7daysGrowthrate) {
    this.infectionsReported7daysGrowthrate = infectionsReported7daysGrowthrate;
  }

  public void setEffectiveDate(String effectiveDate) {
    this.effectiveDate = effectiveDate;
  }

  public void setAppDownloadsDaily(Integer appDownloadsDaily) {
    this.appDownloadsDaily = appDownloadsDaily;
  }

  public void setAppDownloadsCumulated(Integer appDownloadsCumulated) {
    this.appDownloadsCumulated = appDownloadsCumulated;
  }

  public void setAppDownloads7daysSum(Integer appDownloads7daysSum) {
    this.appDownloads7daysSum = appDownloads7daysSum;
  }

  public void setAppDownloads7daysGrowthrate(Integer appDownloads7daysGrowthrate) {
    this.appDownloads7daysGrowthrate = appDownloads7daysGrowthrate;
  }

  public void setAppDownloads7daysTrend5percent(Integer appDownloads7daysTrend5percent) {
    this.appDownloads7daysTrend5percent = appDownloads7daysTrend5percent;
  }

  public void setPersonsWhoSharedKeysDaily(Integer personsWhoSharedKeysDaily) {
    this.personsWhoSharedKeysDaily = personsWhoSharedKeysDaily;
  }

  public void setPersonsWhoSharedKeysCumulated(Integer personsWhoSharedKeysCumulated) {
    this.personsWhoSharedKeysCumulated = personsWhoSharedKeysCumulated;
  }

  public void setPersonsWhoSharedKeys7daysSum(Integer personsWhoSharedKeys7daysSum) {
    this.personsWhoSharedKeys7daysSum = personsWhoSharedKeys7daysSum;
  }

  public void setPersonsWhoSharedKeys7daysGrowthrate(Integer personsWhoSharedKeys7daysGrowthrate) {
    this.personsWhoSharedKeys7daysGrowthrate = personsWhoSharedKeys7daysGrowthrate;
  }

  public void setPersonsWhoSharedKeys7daysTrend5percent(Integer personsWhoSharedKeys7daysTrend5percent) {
    this.personsWhoSharedKeys7daysTrend5percent = personsWhoSharedKeys7daysTrend5percent;
  }

  public void setTestsTotalDaily(Integer testsTotalDaily) {
    this.testsTotalDaily = testsTotalDaily;
  }

  public void setPositiveTestsUsedToShareKeysDaily(Integer positiveTestsUsedToShareKeysDaily) {
    this.positiveTestsUsedToShareKeysDaily = positiveTestsUsedToShareKeysDaily;
  }

  public void setPositiveTestsNotUsedToShareKeysDaily(Integer positiveTestsNotUsedToShareKeysDaily) {
    this.positiveTestsNotUsedToShareKeysDaily = positiveTestsNotUsedToShareKeysDaily;
  }

  public void setLabsRegisteredAbsolut(Integer labsRegisteredAbsolut) {
    this.labsRegisteredAbsolut = labsRegisteredAbsolut;
  }

  public void setLabsConnectedAbsolut(Integer labsConnectedAbsolut) {
    this.labsConnectedAbsolut = labsConnectedAbsolut;
  }

  public void setSevenDayIncidence(Integer sevenDayIncidence) {
    this.sevenDayIncidence = sevenDayIncidence;
  }

  public void setSevenDayIncidenceTrend5percent(Integer sevenDayIncidenceTrend5percent) {
    this.sevenDayIncidenceTrend5percent = sevenDayIncidenceTrend5percent;
  }

  public void setInfectionsReportedDaily(Integer infectionsReportedDaily) {
    this.infectionsReportedDaily = infectionsReportedDaily;
  }

  public void setInfectionsReported7daysAvg(Integer infectionsReported7daysAvg) {
    this.infectionsReported7daysAvg = infectionsReported7daysAvg;
  }


  public void setInfectionsReported7daysTrend5percent(Integer infectionsReported7daysTrend5percent) {
    this.infectionsReported7daysTrend5percent = infectionsReported7daysTrend5percent;
  }

  public void setInfectionsReportedCumulated(Integer infectionsReportedCumulated) {
    this.infectionsReportedCumulated = infectionsReportedCumulated;
  }
}
