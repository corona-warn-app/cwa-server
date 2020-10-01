

package app.coronawarn.server.services.federation.upload.utils;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MockData {

  public static final String TEST_ORIGIN_COUNTRY = "DE";
  public static final SecureRandom random = new SecureRandom();

  public static List<FederationUploadKey> generateRandomUploadKeys(boolean consentToShare, int numberOfKeys) {
    return IntStream.range(0, numberOfKeys)
        .mapToObj(ignore -> generateRandomUploadKey(consentToShare))
        .collect(Collectors.toList());
  }

  public static FederationUploadKey generateRandomUploadKey(boolean consentToShare) {
    return FederationUploadKey.from(generateRandomDiagnosisKey(consentToShare));
  }

  public static List<DiagnosisKey> generateRandomDiagnosisKeys(boolean consentToShare, int numberOfKeys) {
    return IntStream.range(0, numberOfKeys)
        .mapToObj(ignore -> generateRandomDiagnosisKey(consentToShare))
        .collect(Collectors.toList());
  }

  public static DiagnosisKey generateRandomDiagnosisKey(boolean consentToShare) {
    return DiagnosisKey.builder()
        .withKeyData(randomByteData())
        .withRollingStartIntervalNumber(1)
        .withTransmissionRiskLevel(2)
        .withConsentToFederation(consentToShare)
        .withCountryCode(TEST_ORIGIN_COUNTRY)
        .withDaysSinceOnsetOfSymptoms(randomDaysSinceOnsetOfSymptoms())
        .withSubmissionTimestamp(12)
        .withVisitedCountries(Set.of("FR", "DK"))
        .withReportType(ReportType.CONFIRMED_TEST)
        .build();
  }

  private static Integer randomDaysSinceOnsetOfSymptoms() {
    return random.nextInt(13);
  }

  private static byte[] randomByteData() {
    byte[] keyData = new byte[16];
    random.nextBytes(keyData);
    return keyData;
  }
}
