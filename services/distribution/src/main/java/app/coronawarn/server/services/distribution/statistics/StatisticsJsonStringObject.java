package app.coronawarn.server.services.distribution.statistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
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
  private Double infectionsReported7daysAvg;
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
  private Double personWhoSharedKeys7daysAvg;
  @JsonProperty("positive_tests_not_used_to_share_keys_daily")
  private Integer positiveTestsNotUsedToShareKeysDaily;
  @JsonProperty("positive_tests_used_to_share_keys_daily")
  private Integer positiveTestsUsedToShareKeysDaily;
  @JsonProperty("seven_day_incidence_1st_reported_daily")
  private Double sevenDayIncidence;
  @JsonProperty("seven_day_incidence_1st_reported_growthrate")
  private Double sevenDayIncidenceGrowthrate;
  @JsonProperty("seven_day_incidence_1st_reported_trend_1percent")
  private Integer sevenDayIncidenceTrend1percent;
  @JsonProperty("seven_day_r_value_1st_reported_daily")
  private Double sevenDayRvalue1stReportedDaily;
  @JsonProperty("seven_day_r_value_1st_reported_growthrate")
  private Double sevenDayRvalue1stReportedGrowthrate;
  @JsonProperty("seven_day_r_value_1st_reported_trend_1percent")
  private Integer sevenDayRvalue1stReportedTrend1percent;
  @JsonProperty("seven_day_r_value_published_daily")
  private Double sevenDayRvaluepublishedDaily;
  @JsonProperty("seven_day_r_value_published_growthrate")
  private Double sevenDayRvaluepublishedGrowthrate;
  @JsonProperty("seven_day_r_value_published_trend_1percent")
  private Integer sevenDayRvaluePublishedTrend1percent;
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

  public Double getInfectionsReported7daysAvg() {
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

  public Double getPersonWhoSharedKeys7daysAvg() {
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

  public Integer getSevenDayIncidenceTrend1percent() {
    return sevenDayIncidenceTrend1percent;
  }

  public Double getSevenDayRvalue1stReportedDaily() {
    return sevenDayRvalue1stReportedDaily;
  }

  public Double getSevenDayRvalue1stReportedGrowthrate() {
    return sevenDayRvalue1stReportedGrowthrate;
  }

  public Integer getSevenDayRvalue1stReportedTrend1percent() {
    return sevenDayRvalue1stReportedTrend1percent;
  }

  public Double getSevenDayRvaluePublishedDaily() {
    return sevenDayRvaluepublishedDaily;
  }

  public Double getSevenDayRvaluepublishedGrowthrate() {
    return sevenDayRvaluepublishedGrowthrate;
  }

  public Integer getSevenDayRvaluePublishedTrend1percent() {
    return sevenDayRvaluePublishedTrend1percent;
  }

  public Integer getTestsTotalDaily() {
    return testsTotalDaily;
  }

  public String getUpdateTimestamp() {
    return updateTimestamp;
  }

  public void setAppDownloads7DaysAvg(String appDownloads7DaysAvg) {
    this.appDownloads7DaysAvg = appDownloads7DaysAvg;
  }

  public void setAppDownloads7daysGrowthrate(Integer appDownloads7daysGrowthrate) {
    this.appDownloads7daysGrowthrate = appDownloads7daysGrowthrate;
  }

  public void setAppDownloads7daysSum(Integer appDownloads7daysSum) {
    this.appDownloads7daysSum = appDownloads7daysSum;
  }

  public void setAppDownloads7daysTrend5percent(Integer appDownloads7daysTrend5percent) {
    this.appDownloads7daysTrend5percent = appDownloads7daysTrend5percent;
  }

  public void setAppDownloadsCumulated(Integer appDownloadsCumulated) {
    this.appDownloadsCumulated = appDownloadsCumulated;
  }

  public void setAppDownloadsDaily(Integer appDownloadsDaily) {
    this.appDownloadsDaily = appDownloadsDaily;
  }

  public void setEffectiveDate(String effectiveDate) {
    this.effectiveDate = effectiveDate;
  }

  public void setInfectionsReported7daysAvg(Double infectionsReported7daysAvg) {
    this.infectionsReported7daysAvg = infectionsReported7daysAvg;
  }

  public void setInfectionsReported7daysGrowthrate(Double infectionsReported7daysGrowthrate) {
    this.infectionsReported7daysGrowthrate = infectionsReported7daysGrowthrate;
  }

  public void setInfectionsReported7daysTrend5percent(Integer infectionsReported7daysTrend5percent) {
    this.infectionsReported7daysTrend5percent = infectionsReported7daysTrend5percent;
  }

  public void setInfectionsReportedCumulated(Integer infectionsReportedCumulated) {
    this.infectionsReportedCumulated = infectionsReportedCumulated;
  }

  public void setInfectionsReportedDaily(Integer infectionsReportedDaily) {
    this.infectionsReportedDaily = infectionsReportedDaily;
  }

  public void setLabsConnectedAbsolut(Integer labsConnectedAbsolut) {
    this.labsConnectedAbsolut = labsConnectedAbsolut;
  }

  public void setLabsRegisteredAbsolut(Integer labsRegisteredAbsolut) {
    this.labsRegisteredAbsolut = labsRegisteredAbsolut;
  }

  public void setPersonsWhoSharedKeys7daysGrowthrate(Double personsWhoSharedKeys7daysGrowthrate) {
    this.personsWhoSharedKeys7daysGrowthrate = personsWhoSharedKeys7daysGrowthrate;
  }

  public void setPersonsWhoSharedKeys7daysSum(Integer personsWhoSharedKeys7daysSum) {
    this.personsWhoSharedKeys7daysSum = personsWhoSharedKeys7daysSum;
  }

  public void setPersonsWhoSharedKeys7daysTrend5percent(Integer personsWhoSharedKeys7daysTrend5percent) {
    this.personsWhoSharedKeys7daysTrend5percent = personsWhoSharedKeys7daysTrend5percent;
  }

  public void setPersonsWhoSharedKeysCumulated(Integer personsWhoSharedKeysCumulated) {
    this.personsWhoSharedKeysCumulated = personsWhoSharedKeysCumulated;
  }

  public void setPersonsWhoSharedKeysDaily(Integer personsWhoSharedKeysDaily) {
    this.personsWhoSharedKeysDaily = personsWhoSharedKeysDaily;
  }

  public void setPersonWhoSharedKeys7daysAvg(Double personWhoSharedKeys7daysAvg) {
    this.personWhoSharedKeys7daysAvg = personWhoSharedKeys7daysAvg;
  }

  public void setPositiveTestsNotUsedToShareKeysDaily(Integer positiveTestsNotUsedToShareKeysDaily) {
    this.positiveTestsNotUsedToShareKeysDaily = positiveTestsNotUsedToShareKeysDaily;
  }

  public void setPositiveTestsUsedToShareKeysDaily(Integer positiveTestsUsedToShareKeysDaily) {
    this.positiveTestsUsedToShareKeysDaily = positiveTestsUsedToShareKeysDaily;
  }

  public void setSevenDayIncidence(Double sevenDayIncidence) {
    this.sevenDayIncidence = sevenDayIncidence;
  }

  public void setSevenDayIncidenceGrowthrate(Double sevenDayIncidenceGrowthrate) {
    this.sevenDayIncidenceGrowthrate = sevenDayIncidenceGrowthrate;
  }

  public void setSevenDayIncidenceTrend1percent(Integer sevenDayIncidenceTrend5percent) {
    this.sevenDayIncidenceTrend1percent = sevenDayIncidenceTrend5percent;
  }

  public void setSevenDayRvalue1stReportedDaily(Double sevenDayRvalue1stReportedDaily) {
    this.sevenDayRvalue1stReportedDaily = sevenDayRvalue1stReportedDaily;
  }

  public void setSevenDayRvalue1stReportedGrowthrate(Double sevenDayRvalue1stReportedGrowthrate) {
    this.sevenDayRvalue1stReportedGrowthrate = sevenDayRvalue1stReportedGrowthrate;
  }

  public void setSevenDayRvalue1stReportedTrend1percent(Integer sevenDayRvalue1stReportedTrend1percent) {
    this.sevenDayRvalue1stReportedTrend1percent = sevenDayRvalue1stReportedTrend1percent;
  }

  public void setSevenDayRvaluepublishedDaily(Double sevenDayRvaluepublishedDaily) {
    this.sevenDayRvaluepublishedDaily = sevenDayRvaluepublishedDaily;
  }

  public void setSevenDayRvaluepublishedGrowthrate(Double sevenDayRvaluepublishedGrowthrate) {
    this.sevenDayRvaluepublishedGrowthrate = sevenDayRvaluepublishedGrowthrate;
  }

  public void setSevenDayRvaluePublishedTrend1percent(Integer sevenDayRvaluePublishedTrend1percent) {
    this.sevenDayRvaluePublishedTrend1percent = sevenDayRvaluePublishedTrend1percent;
  }

  public void setTestsTotalDaily(Integer testsTotalDaily) {
    this.testsTotalDaily = testsTotalDaily;
  }

  public void setUpdateTimestamp(String updateTimestamp) {
    this.updateTimestamp = updateTimestamp;
  }
}
