package app.coronawarn.server.common.persistence.domain;

import java.util.List;

import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import javax.validation.constraints.Size;

public class FederationUploadKey extends DiagnosisKey{

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

}
