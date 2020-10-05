

package app.coronawarn.server.services.download;

import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "services.download")
@Validated
public class DownloadServiceConfig {

  @Min(0)
  @Max(14)
  private Integer efgsOffsetDays;
  @Min(0)
  @Max(28)
  private Integer retentionDays;
  private TekFieldDerivations tekFieldDerivations;
  @Min(0)
  private int keyLength;
  private List<ReportType> allowedReportTypesToDownload;
  private int minDsos;
  private int maxDsos;
  private int maxRollingPeriod;
  private int minTrl;
  private int maxTrl;

  public int getKeyLength() {
    return keyLength;
  }

  public void setKeyLength(int keyLength) {
    this.keyLength = keyLength;
  }

  public int getMinTrl() {
    return minTrl;
  }

  public void setMinTrl(int minTrl) {
    this.minTrl = minTrl;
  }

  public int getMaxTrl() {
    return maxTrl;
  }

  public void setMaxTrl(int maxTrl) {
    this.maxTrl = maxTrl;
  }

  public int getMaxRollingPeriod() {
    return maxRollingPeriod;
  }

  public void setMaxRollingPeriod(int maxRollingPeriod) {
    this.maxRollingPeriod = maxRollingPeriod;
  }

  public int getMinDsos() {
    return minDsos;
  }

  public void setMinDsos(int minDsos) {
    this.minDsos = minDsos;
  }

  public int getMaxDsos() {
    return maxDsos;
  }

  public void setMaxDsos(int maxDsos) {
    this.maxDsos = maxDsos;
  }

  public List<ReportType> getAllowedReportTypesToDownload() {
    return allowedReportTypesToDownload;
  }

  public void setAllowedReportTypesToDownload(
      List<ReportType> allowedReportTypesToDownload) {
    this.allowedReportTypesToDownload = allowedReportTypesToDownload;
  }

  public TekFieldDerivations getTekFieldDerivations() {
    return tekFieldDerivations;
  }

  public void setTekFieldDerivations(TekFieldDerivations tekFieldDerivations) {
    this.tekFieldDerivations = tekFieldDerivations;
  }

  public Integer getEfgsOffsetDays() {
    return efgsOffsetDays;
  }

  public void setEfgsOffsetDays(Integer efgsOffsetDays) {
    this.efgsOffsetDays = efgsOffsetDays;
  }

  public Integer getRetentionDays() {
    return retentionDays;
  }

  public void setRetentionDays(Integer retentionDays) {
    this.retentionDays = retentionDays;
  }

  public static class TekFieldDerivations {

    @NotNull
    @NotEmpty
    private Map<Integer, Integer> trlFromDsos;

    public Map<Integer, Integer> getTransmissionRiskLevelFromDaysSinceOnsetOfSymptoms() {
      return trlFromDsos;
    }

    public void setTrlFromDsos(Map<Integer, Integer> trlFromDsos) {
      this.trlFromDsos = trlFromDsos;
    }

    public Integer deriveTrlFromDsos(Integer dsos) {
      return trlFromDsos.getOrDefault(dsos, 1);
    }
  }
}
