

package app.coronawarn.server.services.download.config;

import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "services.download")
@Validated
public class DownloadServiceConfig {

  private boolean efgsEnforceDateBasedDownload;
  @Min(0)
  @Max(14)
  private Integer efgsEnforceDownloadOffsetDays;
  @Min(0)
  @Max(28)
  private Integer retentionDays;
  private Validation validation;
  @Autowired
  private TekFieldDerivations tekFieldDerivations;
  private boolean batchAuditEnabled;

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

  public Integer getEfgsEnforceDownloadOffsetDays() {
    return efgsEnforceDownloadOffsetDays;
  }

  public void setEfgsEnforceDownloadOffsetDays(Integer efgsEnforceDownloadOffsetDays) {
    this.efgsEnforceDownloadOffsetDays = efgsEnforceDownloadOffsetDays;
  }

  public boolean getEfgsEnforceDateBasedDownload() {
    return efgsEnforceDateBasedDownload;
  }

  public void setEfgsEnforceDateBasedDownload(boolean efgsEnforceDateBasedDownload) {
    this.efgsEnforceDateBasedDownload = efgsEnforceDateBasedDownload;
  }

  public Integer getRetentionDays() {
    return retentionDays;
  }

  public void setRetentionDays(Integer retentionDays) {
    this.retentionDays = retentionDays;
  }

  public boolean isBatchAuditEnabled() {
    return batchAuditEnabled;
  }

  public void setBatchAuditEnabled(boolean batchAuditEnabled) {
    this.batchAuditEnabled = batchAuditEnabled;
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

  @Configuration
  public static class SslConfig {

    @Autowired
    private Environment env;

    @PostConstruct
    private void configureSsl() {
      // load the 'javax.net.ssl.trustStore' and
      // 'javax.net.ssl.trustStorePassword' from application.properties
      String trustStore = env.getProperty("server.ssl.trust-store");
      if (!Strings.isBlank(trustStore) && !trustStore.startsWith("${")) {
        System.setProperty("javax.net.ssl.trustStore", env.getProperty("server.ssl.trust-store"));
        System.setProperty("javax.net.ssl.trustStorePassword", env.getProperty("server.ssl.trust-store-password"));
      }
    }
  }
}
