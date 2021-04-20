

package app.coronawarn.server.services.federation.upload.utils;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MockData {

  public static final String TEST_ORIGIN_COUNTRY = "DE";
  public static final SecureRandom random = new SecureRandom();

  public static List<FederationUploadKey> generateRandomUploadKeys(boolean consentToShare, int numberOfKeys,
      SubmissionType submissionType) {
    return IntStream.range(0, numberOfKeys)
        .mapToObj(ignore -> generateRandomUploadKey(consentToShare, submissionType))
        .collect(Collectors.toList());
  }

  public static FederationUploadKey generateRandomUploadKey(boolean consentToShare, SubmissionType submissionType) {
    return FederationUploadKey
        .from(generateRandomDiagnosisKey(consentToShare, submissionType));
  }

  public static List<DiagnosisKey> generateRandomDiagnosisKeys(boolean consentToShare, int numberOfKeys) {
    return IntStream.range(0, numberOfKeys)
        .mapToObj(ignore -> generateRandomDiagnosisKey(consentToShare, SubmissionType.SUBMISSION_TYPE_PCR_TEST))
        .collect(Collectors.toList());
  }

  public static int makeRollingStartIntervalFromSubmission(long submissionTimestamp) {
    return (int) ((submissionTimestamp) * 6);
  }

  public static DiagnosisKey generateRandomDiagnosisKey(boolean consentToShare, SubmissionType submissionType) {
    var timestamp = LocalDateTime.now(ZoneOffset.UTC)
        .minusDays(2L)
        .truncatedTo(ChronoUnit.HOURS)
        .toEpochSecond(ZoneOffset.UTC) / 3600;
    return generateRandomDiagnosisKey(consentToShare, timestamp, submissionType);
  }

  public static DiagnosisKey generateRandomDiagnosisKey(boolean consentToShare, long submissionTimestamp,
      SubmissionType submissionType) {
    return DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(randomByteData(), submissionType)
        .withRollingStartIntervalNumber(makeRollingStartIntervalFromSubmission(submissionTimestamp))
        .withTransmissionRiskLevel(2)
        .withConsentToFederation(consentToShare)
        .withCountryCode(TEST_ORIGIN_COUNTRY)
        .withDaysSinceOnsetOfSymptoms(randomDaysSinceOnsetOfSymptoms())
        .withSubmissionTimestamp(submissionTimestamp)
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
