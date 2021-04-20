package app.coronawarn.server.services.federation.upload.testdata;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;

public interface TestDataUploadRepository {

  void storeUploadKey(byte[] keyData, int rollingStartIntervalNumber, int rollingPeriod,
      long submissionTimestamp, int transmissionRisk, String originCountry,
      String[] visitedCountries, String reportType, int daysSinceOnsetOfSymptoms,
      boolean consentToFederation, String batchTag, SubmissionType submissionType);

  Integer countPendingKeys();

  void applyRetentionToTestKeys(int retention);
}
