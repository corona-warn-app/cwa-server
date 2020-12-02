

package app.coronawarn.server.services.federation.upload.utils;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UploadKeysMockData {

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

  public static FederationUploadKey generateRandomUploadKey(ReportType reportType) {
    return FederationUploadKey.from(generateRandomDiagnosisKey(reportType));
  }

  public static FederationUploadKey generateRandomUploadKey(int daysSinceOnsetSymptoms) {
    return FederationUploadKey.from(generateRandomDiagnosisKey(daysSinceOnsetSymptoms));
  }

  public static FederationUploadKey generateRandomUploadKey(String originCountry,
      Set<String> visitedCountries) {
    return FederationUploadKey.from(generateRandomDiagnosisKey(originCountry, visitedCountries));
  }

  public static List<DiagnosisKey> generateRandomDiagnosisKeys(boolean consentToShare, int numberOfKeys) {
    return IntStream.range(0, numberOfKeys)
        .mapToObj(ignore -> generateRandomDiagnosisKey(consentToShare))
        .collect(Collectors.toList());
  }

  public static int makeRollingStartIntervalFromSubmission(long submissionTimestamp) {
    return (int)((submissionTimestamp) * 6);
  }

  public static DiagnosisKey generateRandomDiagnosisKey(boolean consentToShare) {
    var timestamp = LocalDateTime.now(ZoneOffset.UTC)
        .minusDays(2L)
        .truncatedTo(ChronoUnit.HOURS)
        .toEpochSecond(ZoneOffset.UTC) / 3600;
    return generateRandomDiagnosisKey(consentToShare, timestamp);
  }

  public static DiagnosisKey generateRandomDiagnosisKey(String originCountry,
      Set<String> visitedCountries) {
    var timestamp = LocalDateTime.now(ZoneOffset.UTC)
        .minusDays(2L)
        .truncatedTo(ChronoUnit.HOURS)
        .toEpochSecond(ZoneOffset.UTC) / 3600;
    return generateRandomDiagnosisKey(true, timestamp, originCountry, visitedCountries);
  }

  public static DiagnosisKey generateRandomDiagnosisKey(ReportType reportType) {
    var timestamp = LocalDateTime.now(ZoneOffset.UTC)
        .minusDays(2L)
        .truncatedTo(ChronoUnit.HOURS)
        .toEpochSecond(ZoneOffset.UTC) / 3600;
    return generateRandomDiagnosisKey(true, timestamp, TEST_ORIGIN_COUNTRY, Set.of("FR", "DK"), reportType);
  }

  public static DiagnosisKey generateRandomDiagnosisKey(boolean consentToShare, long submissionTimestamp) {
    return generateRandomDiagnosisKey(consentToShare, submissionTimestamp, TEST_ORIGIN_COUNTRY, Set.of("FR", "DK"));
  }

  public static DiagnosisKey generateRandomDiagnosisKey(boolean consentToShare,
      long submissionTimestamp, String originCountry, Set<String> visitedCountries) {
    return generateRandomDiagnosisKey(consentToShare, submissionTimestamp, originCountry,
        visitedCountries, ReportType.CONFIRMED_TEST);
  }

  public static DiagnosisKey generateRandomDiagnosisKey(int daysSinceOnsetOfSymptoms) {
    var timestamp = LocalDateTime.now(ZoneOffset.UTC)
        .minusDays(2L)
        .truncatedTo(ChronoUnit.HOURS)
        .toEpochSecond(ZoneOffset.UTC) / 3600;
    return DiagnosisKey.builder()
        .withKeyData(randomByteData())
        .withRollingStartIntervalNumber(makeRollingStartIntervalFromSubmission(timestamp))
        .withTransmissionRiskLevel(2)
        .withConsentToFederation(true)
        .withCountryCode("DE")
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withSubmissionTimestamp(timestamp)
        .withVisitedCountries(Set.of("FR", "DK"))
        .withReportType(ReportType.CONFIRMED_TEST)
        .build();
  }

  public static DiagnosisKey generateRandomDiagnosisKey(boolean consentToShare,
      long submissionTimestamp, String originCountry, Set<String> visitedCountries, ReportType reportType) {
    return DiagnosisKey.builder()
        .withKeyData(randomByteData())
        .withRollingStartIntervalNumber(makeRollingStartIntervalFromSubmission(submissionTimestamp))
        .withTransmissionRiskLevel(2)
        .withConsentToFederation(consentToShare)
        .withCountryCode(originCountry)
        .withDaysSinceOnsetOfSymptoms(randomDaysSinceOnsetOfSymptoms())
        .withSubmissionTimestamp(submissionTimestamp)
        .withVisitedCountries(visitedCountries)
        .withReportType(reportType)
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
