package app.coronawarn.server.services.federation.upload.testdata;

public interface TestDataUploadRepository {

  void storeUploadKey(byte[] keyData, int rollingStartIntervalNumber, int rollingPeriod,
      long submissionTimestamp, int transmissionRisk, String originCountry,
      String[] visitedCountries, String reportType, int daysSinceOnsetOfSymptoms,
      boolean consentToFederation);

  Integer countPendingKeys();

  void applyRetentionToTestKeys(int retention);
}
