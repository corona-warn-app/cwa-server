package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import org.springframework.stereotype.Component;

@Component
public class AllowedPropertiesMap {

  private final boolean canSendDsos;
  private final boolean canSendReportType;
  private final Integer defaultDsos;
  private final ReportType defaultReportType;

  /**
   * Reads configuration and returns the received value if properties are allowed to be sent to EFGS. Returns a default
   * otherwise.
   * @param configuration {@link UploadServiceConfig} Upload configuration.
   */
  public AllowedPropertiesMap(UploadServiceConfig configuration) {
    this.canSendDsos = configuration.getEfgsTransmission().isEnableDsos();
    this.canSendReportType = configuration.getEfgsTransmission().isEnableReportType();
    this.defaultDsos = configuration.getEfgsTransmission().getDefaultDsos();
    this.defaultReportType = configuration.getEfgsTransmission().getDefaultReportType();
  }

  public int getDsosOrDefault(int dsos) {
    return canSendDsos ? dsos : defaultDsos;
  }

  public ReportType getReportTypeOrDefault(ReportType type) {
    return canSendReportType ? type : defaultReportType;
  }

}
