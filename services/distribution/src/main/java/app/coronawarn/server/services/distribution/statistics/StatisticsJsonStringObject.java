package app.coronawarn.server.services.distribution.statistics;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class StatisticsJsonStringObject {

  @JsonProperty("app_downloads_7days_avg")
  private String appDownloads7DaysAvg;
  @JsonProperty("app_downloads_7days_avg_growthrate")
  private Integer appDownloads7daysGrowthrate;
  @JsonProperty("app_downloads_7days_sum")
  private Integer appDownloads7daysSum;
  @JsonProperty("app_downloads_7days_avg_trend_5percent")
  private Integer appDownloads7daysTrend5percent;
  @JsonProperty("app_downloads_cumulated")
  private Integer appDownloadsCumulated;
  @JsonProperty("app_downloads_daily")
  private Integer appDownloadsDaily;
  @JsonProperty("effective_date")
  private String effectiveDate;
  @JsonProperty("infections_effective_7days_avg")
  private Integer infectionsReported7daysAvg;
  @JsonProperty("infections_effective_7days_avg_growthrate")
  private Double infectionsReported7daysGrowthrate;
  @JsonProperty("infections_effective_7days_avg_trend_5percent")
  private Integer infectionsReported7daysTrend5percent;
  @JsonProperty("infections_effective_cumulated")
  private Integer infectionsReportedCumulated;
  @JsonProperty("infections_effective_daily")
  private Integer infectionsReportedDaily;
  @JsonProperty("labs_connected_cumulated")
  private Integer labsConnectedAbsolut;
  @JsonProperty("labs_registered_cumulated")
  private Integer labsRegisteredAbsolut;
  @JsonProperty("persons_who_shared_keys_7days_avg_growthrate")
  private Double personsWhoSharedKeys7daysGrowthrate;
  @JsonProperty("persons_who_shared_keys_7days_sum")
  private Integer personsWhoSharedKeys7daysSum;
  @JsonProperty("persons_who_shared_keys_7days_avg_trend_5percent")
  private Integer personsWhoSharedKeys7daysTrend5percent;
  @JsonProperty("persons_who_shared_keys_cumulated")
  private Integer personsWhoSharedKeysCumulated;
  @JsonProperty("persons_who_shared_keys_daily")
  private Integer personsWhoSharedKeysDaily;
  @JsonProperty("persons_who_shared_keys_7days_avg")
  private Integer personWhoSharedKeys7daysAvg;
  @JsonProperty("positive_tests_not_used_to_share_keys_daily")
  private Integer positiveTestsNotUsedToShareKeysDaily;
  @JsonProperty("positive_tests_used_to_share_keys_daily")
  private Integer positiveTestsUsedToShareKeysDaily;
  @JsonProperty("seven_day_incidence_1st_reported_daily")
  private Double sevenDayIncidence;
  @JsonProperty("seven_day_incidence_1st_reported_growthrate")
  private Double sevenDayIncidenceGrowthrate;
  @JsonProperty("seven_day_incidence_1st_reported_trend_1percent")
  private Integer sevenDayIncidenceTrend5percent;
  @JsonProperty("seven_day_r_value_1st_reported_daily")
  private Double sevenDayRvalue1stReportedDaily;
  @JsonProperty("seven_day_r_value_1st_reported_growthrate")
  private Double sevenDayRvalue1stReportedGrowthrate;
  @JsonProperty("seven_day_r_value_1st_reported_trend_1percent")
  private Integer sevenDayRvalue1stReportedTrend5percent;
  @JsonProperty("seven_day_r_value_published_daily")
  private Double sevenDayRvaluepublishedDaily;
  @JsonProperty("seven_day_r_value_published_growthrate")
  private Double sevenDayRvaluepublishedGrowthrate;
  @JsonProperty("seven_day_r_value_published_trend_1percent")
  private Integer sevenDayRvaluePublishedTrend5percent;
  @JsonProperty("tests_total_daily")
  private Integer testsTotalDaily;
  @JsonProperty("update_timestamp")
  private String updateTimestamp;

  public String getAppDownloads7DaysAvg() {
    return appDownloads7DaysAvg;
  }

  public Integer getAppDownloads7daysGrowthrate() {
    return appDownloads7daysGrowthrate;
  }

  public Integer getAppDownloads7daysSum() {
    return appDownloads7daysSum;
  }

  public Integer getAppDownloads7daysTrend5percent() {
    return appDownloads7daysTrend5percent;
  }

  public Integer getAppDownloadsCumulated() {
    return appDownloadsCumulated;
  }

  public Integer getAppDownloadsDaily() {
    return appDownloadsDaily;
  }

  public String getEffectiveDate() {
    return effectiveDate;
  }

  public Integer getInfectionsReported7daysAvg() {
    return infectionsReported7daysAvg;
  }

  public Double getInfectionsReported7daysGrowthrate() {
    return infectionsReported7daysGrowthrate;
  }

  public Integer getInfectionsReported7daysTrend5percent() {
    return infectionsReported7daysTrend5percent;
  }

  public Integer getInfectionsReportedCumulated() {
    return infectionsReportedCumulated;
  }

  public Integer getInfectionsReportedDaily() {
    return infectionsReportedDaily;
  }

  public Integer getLabsConnectedAbsolut() {
    return labsConnectedAbsolut;
  }

  public Integer getLabsRegisteredAbsolut() {
    return labsRegisteredAbsolut;
  }

  public Double getPersonsWhoSharedKeys7daysGrowthrate() {
    return personsWhoSharedKeys7daysGrowthrate;
  }

  public Integer getPersonsWhoSharedKeys7daysSum() {
    return personsWhoSharedKeys7daysSum;
  }

  public Integer getPersonsWhoSharedKeys7daysTrend5percent() {
    return personsWhoSharedKeys7daysTrend5percent;
  }

  public Integer getPersonsWhoSharedKeysCumulated() {
    return personsWhoSharedKeysCumulated;
  }

  public Integer getPersonsWhoSharedKeysDaily() {
    return personsWhoSharedKeysDaily;
  }

  public Integer getPersonWhoSharedKeys7daysAvg() {
    return personWhoSharedKeys7daysAvg;
  }

  public Integer getPositiveTestsNotUsedToShareKeysDaily() {
    return positiveTestsNotUsedToShareKeysDaily;
  }

  public Integer getPositiveTestsUsedToShareKeysDaily() {
    return positiveTestsUsedToShareKeysDaily;
  }

  public Double getSevenDayIncidence() {
    return sevenDayIncidence;
  }

  public Double getSevenDayIncidenceGrowthrate() {
    return sevenDayIncidenceGrowthrate;
  }

  public Integer getSevenDayIncidenceTrend5percent() {
    return sevenDayIncidenceTrend5percent;
  }

  public Double getSevenDayRvalue1stReportedDaily() {
    return sevenDayRvalue1stReportedDaily;
  }

  public Double getSevenDayRvalue1stReportedGrowthrate() {
    return sevenDayRvalue1stReportedGrowthrate;
  }

  public Integer getSevenDayRvalue1stReportedTrend5percent() {
    return sevenDayRvalue1stReportedTrend5percent;
  }

  public Double getSevenDayRvaluepublishedDaily() {
    return sevenDayRvaluepublishedDaily;
  }

  public Double getSevenDayRvaluepublishedGrowthrate() {
    return sevenDayRvaluepublishedGrowthrate;
  }

  public Integer getSevenDayRvaluePublishedTrend5percent() {
    return sevenDayRvaluePublishedTrend5percent;
  }

  public Integer getTestsTotalDaily() {
    return testsTotalDaily;
  }

  public String getUpdateTimestamp() {
    return updateTimestamp;
  }

  public void setAppDownloads7DaysAvg(final String appDownloads7DaysAvg) {
    this.appDownloads7DaysAvg = appDownloads7DaysAvg;
  }

  public void setAppDownloads7daysGrowthrate(final Integer appDownloads7daysGrowthrate) {
    this.appDownloads7daysGrowthrate = appDownloads7daysGrowthrate;
  }

  public void setAppDownloads7daysSum(final Integer appDownloads7daysSum) {
    this.appDownloads7daysSum = appDownloads7daysSum;
  }

  public void setAppDownloads7daysTrend5percent(final Integer appDownloads7daysTrend5percent) {
    this.appDownloads7daysTrend5percent = appDownloads7daysTrend5percent;
  }

  public void setAppDownloadsCumulated(final Integer appDownloadsCumulated) {
    this.appDownloadsCumulated = appDownloadsCumulated;
  }

  public void setAppDownloadsDaily(final Integer appDownloadsDaily) {
    this.appDownloadsDaily = appDownloadsDaily;
  }

  public void setEffectiveDate(final String effectiveDate) {
    this.effectiveDate = effectiveDate;
  }

  public void setInfectionsReported7daysAvg(final Integer infectionsReported7daysAvg) {
    this.infectionsReported7daysAvg = infectionsReported7daysAvg;
  }

  public void setInfectionsReported7daysGrowthrate(final Double infectionsReported7daysGrowthrate) {
    this.infectionsReported7daysGrowthrate = infectionsReported7daysGrowthrate;
  }

  public void setInfectionsReported7daysTrend5percent(final Integer infectionsReported7daysTrend5percent) {
    this.infectionsReported7daysTrend5percent = infectionsReported7daysTrend5percent;
  }

  public void setInfectionsReportedCumulated(final Integer infectionsReportedCumulated) {
    this.infectionsReportedCumulated = infectionsReportedCumulated;
  }

  public void setInfectionsReportedDaily(final Integer infectionsReportedDaily) {
    this.infectionsReportedDaily = infectionsReportedDaily;
  }

  public void setLabsConnectedAbsolut(final Integer labsConnectedAbsolut) {
    this.labsConnectedAbsolut = labsConnectedAbsolut;
  }

  public void setLabsRegisteredAbsolut(final Integer labsRegisteredAbsolut) {
    this.labsRegisteredAbsolut = labsRegisteredAbsolut;
  }

  public void setPersonsWhoSharedKeys7daysGrowthrate(final Double personsWhoSharedKeys7daysGrowthrate) {
    this.personsWhoSharedKeys7daysGrowthrate = personsWhoSharedKeys7daysGrowthrate;
  }

  public void setPersonsWhoSharedKeys7daysSum(final Integer personsWhoSharedKeys7daysSum) {
    this.personsWhoSharedKeys7daysSum = personsWhoSharedKeys7daysSum;
  }

  public void setPersonsWhoSharedKeys7daysTrend5percent(final Integer personsWhoSharedKeys7daysTrend5percent) {
    this.personsWhoSharedKeys7daysTrend5percent = personsWhoSharedKeys7daysTrend5percent;
  }

  public void setPersonsWhoSharedKeysCumulated(final Integer personsWhoSharedKeysCumulated) {
    this.personsWhoSharedKeysCumulated = personsWhoSharedKeysCumulated;
  }

  public void setPersonsWhoSharedKeysDaily(final Integer personsWhoSharedKeysDaily) {
    this.personsWhoSharedKeysDaily = personsWhoSharedKeysDaily;
  }

  public void setPersonWhoSharedKeys7daysAvg(final Integer personWhoSharedKeys7daysAvg) {
    this.personWhoSharedKeys7daysAvg = personWhoSharedKeys7daysAvg;
  }

  public void setPositiveTestsNotUsedToShareKeysDaily(final Integer positiveTestsNotUsedToShareKeysDaily) {
    this.positiveTestsNotUsedToShareKeysDaily = positiveTestsNotUsedToShareKeysDaily;
  }

  public void setPositiveTestsUsedToShareKeysDaily(final Integer positiveTestsUsedToShareKeysDaily) {
    this.positiveTestsUsedToShareKeysDaily = positiveTestsUsedToShareKeysDaily;
  }

  public void setSevenDayIncidence(final Double sevenDayIncidence) {
    this.sevenDayIncidence = sevenDayIncidence;
  }

  public void setSevenDayIncidenceGrowthrate(final Double sevenDayIncidenceGrowthrate) {
    this.sevenDayIncidenceGrowthrate = sevenDayIncidenceGrowthrate;
  }

  public void setSevenDayIncidenceTrend5percent(final Integer sevenDayIncidenceTrend5percent) {
    this.sevenDayIncidenceTrend5percent = sevenDayIncidenceTrend5percent;
  }

  public void setSevenDayRvalue1stReportedDaily(final Double sevenDayRvalue1stReportedDaily) {
    this.sevenDayRvalue1stReportedDaily = sevenDayRvalue1stReportedDaily;
  }

  public void setSevenDayRvalue1stReportedGrowthrate(final Double sevenDayRvalue1stReportedGrowthrate) {
    this.sevenDayRvalue1stReportedGrowthrate = sevenDayRvalue1stReportedGrowthrate;
  }

  public void setSevenDayRvalue1stReportedTrend5percent(final Integer sevenDayRvalue1stReportedTrend5percent) {
    this.sevenDayRvalue1stReportedTrend5percent = sevenDayRvalue1stReportedTrend5percent;
  }

  public void setSevenDayRvaluepublishedDaily(final Double sevenDayRvaluepublishedDaily) {
    this.sevenDayRvaluepublishedDaily = sevenDayRvaluepublishedDaily;
  }

  public void setSevenDayRvaluepublishedGrowthrate(final Double sevenDayRvaluepublishedGrowthrate) {
    this.sevenDayRvaluepublishedGrowthrate = sevenDayRvaluepublishedGrowthrate;
  }

  public void setSevenDayRvaluePublishedTrend5percent(final Integer sevenDayRvaluePublishedTrend5percent) {
    this.sevenDayRvaluePublishedTrend5percent = sevenDayRvaluePublishedTrend5percent;
  }

  public void setTestsTotalDaily(final Integer testsTotalDaily) {
    this.testsTotalDaily = testsTotalDaily;
  }

  public void setUpdateTimestamp(final String updateTimestamp) {
    this.updateTimestamp = updateTimestamp;
  }
}
