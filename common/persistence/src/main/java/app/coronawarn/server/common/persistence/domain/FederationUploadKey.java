package app.coronawarn.server.common.persistence.domain;

import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.util.List;
import javax.validation.constraints.Size;

public class FederationUploadKey extends DiagnosisKey {

  private String batchTagId;

  FederationUploadKey(byte[] keyData, int rollingStartIntervalNumber, int rollingPeriod, int transmissionRiskLevel,
      long submissionTimestamp, boolean consentToFederation, @Size String originCountry, List<String> visitedCountries,
      ReportType reportType, int daysSinceOnsetOfSymptoms) {
    super(keyData, rollingStartIntervalNumber, rollingPeriod, transmissionRiskLevel, submissionTimestamp,
        consentToFederation, originCountry, visitedCountries, reportType, daysSinceOnsetOfSymptoms);
  }

  public String getBatchTagId() {
    return batchTagId;
  }

  public void setBatchTagId(String batchTagId) {
    this.batchTagId = batchTagId;
  }

  /**
   * Create a new instance of an upload key by copying the properties of the given source diagnosis key.
   */
  public static FederationUploadKey from(DiagnosisKey diagnosisKeySource) {
    return new FederationUploadKey(diagnosisKeySource.getKeyData(), diagnosisKeySource.getRollingStartIntervalNumber(),
        diagnosisKeySource.getRollingPeriod(), diagnosisKeySource.getTransmissionRiskLevel(),
        diagnosisKeySource.getSubmissionTimestamp(), diagnosisKeySource.isConsentToFederation(),
        diagnosisKeySource.getOriginCountry(), diagnosisKeySource.getVisitedCountries(),
        diagnosisKeySource.getReportType(), diagnosisKeySource.getDaysSinceOnsetOfSymptoms());
  }

}
