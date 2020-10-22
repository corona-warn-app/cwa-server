

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
  @Max(14)
  private Integer efgsEnforceDownloadOffsetDays;
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

  public Integer getEfgsEnforceDownloadOffsetDays() {
    return efgsEnforceDownloadOffsetDays;
  }

  public void setEfgsEnforceDownloadOffsetDays(Integer efgsEnforceDownloadOffsetDays) {
    this.efgsEnforceDownloadOffsetDays = efgsEnforceDownloadOffsetDays;
  }

  public Integer getRetentionDays() {
    return retentionDays;
  }

  public void setRetentionDays(Integer retentionDays) {
    this.retentionDays = retentionDays;
  }

  public static class Validation {

    private List<ReportType> allowedReportTypes;

    public List<ReportType> getAllowedReportTypes() {
      return allowedReportTypes;
    }

    public void setAllowedReportTypes(
        List<ReportType> allowedReportTypes) {
      this.allowedReportTypes = allowedReportTypes;
    }
  }
}
