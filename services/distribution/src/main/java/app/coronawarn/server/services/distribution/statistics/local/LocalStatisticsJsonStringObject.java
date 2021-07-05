package app.coronawarn.server.services.distribution.statistics.local;

import app.coronawarn.server.services.distribution.statistics.StatisticsTimeJsonObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalStatisticsJsonStringObject extends StatisticsTimeJsonObject {

  @JsonProperty("province_code")
  private String provinceCode;

  @JsonProperty("province_name")
  private String provinceName;

  @JsonProperty("seven_day_incidence_1st_reported_daily")
  private Double sevenDayIncidence1stReportedDaily;

  @JsonProperty("seven_day_incidence_1st_reported_growthrate")
  private Double sevenDayIncidence1stReportedGrowthrate;

  @JsonProperty("seven_day_incidence_1st_reported_trend_1percent")
  private Integer sevenDayIncidence1stReportedTrend1Percent;

  public String getProvinceCode() {
    return provinceCode;
  }

  public void setProvinceCode(String provinceCode) {
    this.provinceCode = provinceCode;
  }

  public String getProvinceName() {
    return provinceName;
  }

  public void setProvinceName(String provinceName) {
    this.provinceName = provinceName;
  }

  public Double getSevenDayIncidence1stReportedDaily() {
    return sevenDayIncidence1stReportedDaily == null ? Double.valueOf(0.0) : sevenDayIncidence1stReportedDaily;
  }

  public void setSevenDayIncidence1stReportedDaily(Double sevenDayIncidence1stReportedDaily) {
    this.sevenDayIncidence1stReportedDaily = sevenDayIncidence1stReportedDaily;
  }

  public void setSevenDayIncidence1stReportedGrowthrate(Double sevenDayIncidence1stReportedGrowthrate) {
    this.sevenDayIncidence1stReportedGrowthrate = sevenDayIncidence1stReportedGrowthrate;
  }

  public Integer getSevenDayIncidence1stReportedTrend1Percent() {
    return sevenDayIncidence1stReportedTrend1Percent == null ? Integer.valueOf(0)
        : sevenDayIncidence1stReportedTrend1Percent;
  }

  public void setSevenDayIncidence1stReportedTrend1Percent(Integer sevenDayIncidence1stReportedTrend1Percent) {
    this.sevenDayIncidence1stReportedTrend1Percent = sevenDayIncidence1stReportedTrend1Percent;
  }

  /**
   * Check if this object is valid and complete.
   * 
   * @return <code>true</code> if and only it {@link #sevenDayIncidence1stReportedDaily} is NOT null and
   *         {@link #sevenDayIncidence1stReportedTrend1Percent} is NOT null.
   */
  public boolean isComplete() {
    return sevenDayIncidence1stReportedDaily != null && sevenDayIncidence1stReportedTrend1Percent != null;
  }
}
