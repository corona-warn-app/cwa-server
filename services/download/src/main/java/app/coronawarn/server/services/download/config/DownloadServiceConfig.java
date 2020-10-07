

package app.coronawarn.server.services.download.config;

import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
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
  private Validation validation;
  @Autowired
  private TekFieldDerivations tekFieldDerivations;

  public Validation getValidation() {
    return validation;
  }

  public void setValidation(
      Validation validation) {
    this.validation = validation;
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

  public static class Validation {

    @Min(0)
    private int keyLength;
    private List<ReportType> allowedReportTypes;
    private int minDsos;
    private int maxDsos;
    private int minRollingPeriod;
    private int maxRollingPeriod;
    private int minTrl;
    private int maxTrl;

    public int getKeyLength() {
      return keyLength;
    }

    public void setKeyLength(int keyLength) {
      this.keyLength = keyLength;
    }

    public List<ReportType> getAllowedReportTypes() {
      return allowedReportTypes;
    }

    public void setAllowedReportTypes(
        List<ReportType> allowedReportTypes) {
      this.allowedReportTypes = allowedReportTypes;
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

    public int getMinRollingPeriod() {
      return minRollingPeriod;
    }

    public void setMinRollingPeriod(int minRollingPeriod) {
      this.minRollingPeriod = minRollingPeriod;
    }

    public int getMaxRollingPeriod() {
      return maxRollingPeriod;
    }

    public void setMaxRollingPeriod(int maxRollingPeriod) {
      this.maxRollingPeriod = maxRollingPeriod;
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
  }
}
