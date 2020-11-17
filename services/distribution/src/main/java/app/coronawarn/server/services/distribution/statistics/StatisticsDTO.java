package app.coronawarn.server.services.distribution.statistics;

public class StatisticsDTO {
  private String date;

  private Integer app_downloads_cum ;

  private Integer key_uploads;

  private Integer test_total;

  private Integer total_redeemed ;

  private Integer total_not_redeemed;

  private Integer labs_total;

  private Integer labs_done;

  private Integer infections ;

  private String day_of_week ;

  private Integer week_of_year;

  public String getDate() {
    return date;
  }

  public Integer getApp_downloads_cum() {
    return app_downloads_cum;
  }

  public Integer getKey_uploads() {
    return key_uploads;
  }

  public Integer getTest_total() {
    return test_total;
  }

  public Integer getTotal_redeemed() {
    return total_redeemed;
  }

  public Integer getTotal_not_redeemed() {
    return total_not_redeemed;
  }

  public Integer getLabs_total() {
    return labs_total;
  }

  public Integer getLabs_done() {
    return labs_done;
  }

  public Integer getInfections() {
    return infections;
  }

  public String getDay_of_week() {
    return day_of_week;
  }

  public Integer getWeek_of_year() {
    return week_of_year;
  }

}
