package app.coronawarn.server.services.submission.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@Sql(scripts = {"classpath:db/clean_db_state.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class DiagnosisKeyInsertionIT {

  public static final SecureRandom random = new SecureRandom();

  @Autowired
  private DiagnosisKeyService keyService;

  @Autowired
  private DiagnosisKeyRepository keyRepository;

  @Test
  void insertsDiagnosisKeys() {
    keyService.saveDiagnosisKeys(List.of(
        generateRandomDiagnosisKey(false, 1, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        generateRandomDiagnosisKey(false, 1, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        generateRandomDiagnosisKey(true, 1, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        generateRandomDiagnosisKey(true, 1, SubmissionType.SUBMISSION_TYPE_RAPID_TEST)
    ));
    Collection<DiagnosisKey> storedKeys = keyService.getDiagnosisKeys();
    assertEquals(4, storedKeys.size());
  }

  @Test
  void insertsPcrTestDiagnosisKeysWhenRapidTestIsPresent() {
    DiagnosisKey pcrKey = generateRandomDiagnosisKey(true, 1, SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    DiagnosisKey rapidKey = DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(pcrKey.getKeyData(), SubmissionType.SUBMISSION_TYPE_RAPID_TEST)
        .withRollingStartIntervalNumber(pcrKey.getRollingStartIntervalNumber())
        .withTransmissionRiskLevel(pcrKey.getTransmissionRiskLevel())
        .withConsentToFederation(pcrKey.isConsentToFederation())
        .withCountryCode(pcrKey.getOriginCountry())
        .withDaysSinceOnsetOfSymptoms(pcrKey.getDaysSinceOnsetOfSymptoms())
        .withReportType(pcrKey.getReportType())
        .withRollingPeriod(pcrKey.getRollingPeriod())
        .withSubmissionTimestamp(pcrKey.getSubmissionTimestamp())
        .withVisitedCountries(pcrKey.getVisitedCountries())
        .build();
    Collection<DiagnosisKey> storedKeys;
    keyService.saveDiagnosisKeys(List.of(rapidKey));
    storedKeys = keyService.getDiagnosisKeys();
    assertEquals(1, storedKeys.size());
    assertTrue(storedKeys.contains(rapidKey));
    keyService.saveDiagnosisKeys(List.of(pcrKey));
    storedKeys = keyService.getDiagnosisKeys();
    assertEquals(2, storedKeys.size());
    assertTrue(storedKeys.contains(rapidKey));
    assertTrue(storedKeys.contains(pcrKey));
  }

  @Test
  void ignoresRapidTestDiagnosisKeysWhenPcrTestIsPresent() {
    DiagnosisKey pcrKey = generateRandomDiagnosisKey(true, 1, SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    DiagnosisKey rapidKey = DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(pcrKey.getKeyData(), SubmissionType.SUBMISSION_TYPE_RAPID_TEST)
        .withRollingStartIntervalNumber(pcrKey.getRollingStartIntervalNumber())
        .withTransmissionRiskLevel(pcrKey.getTransmissionRiskLevel())
        .withConsentToFederation(pcrKey.isConsentToFederation())
        .withCountryCode(pcrKey.getOriginCountry())
        .withDaysSinceOnsetOfSymptoms(pcrKey.getDaysSinceOnsetOfSymptoms())
        .withReportType(pcrKey.getReportType())
        .withRollingPeriod(pcrKey.getRollingPeriod())
        .withSubmissionTimestamp(pcrKey.getSubmissionTimestamp())
        .withVisitedCountries(pcrKey.getVisitedCountries())
        .build();
    Collection<DiagnosisKey> storedKeys;
    keyService.saveDiagnosisKeys(List.of(pcrKey));
    storedKeys = keyService.getDiagnosisKeys();
    assertEquals(1, storedKeys.size());
    assertTrue(storedKeys.contains(pcrKey));
    keyService.saveDiagnosisKeys(List.of(rapidKey));
    storedKeys = keyService.getDiagnosisKeys();
    assertEquals(1, storedKeys.size());
    assertTrue(storedKeys.contains(pcrKey));
    assertFalse(storedKeys.contains(rapidKey));
  }

  public static DiagnosisKey generateRandomDiagnosisKey(boolean consentToShare, long submissionTimestamp,
      SubmissionType submissionType) {
    return DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(randomByteData(), submissionType)
        .withRollingStartIntervalNumber((int) submissionTimestamp * 6)
        .withTransmissionRiskLevel(2)
        .withConsentToFederation(consentToShare)
        .withCountryCode("DE")
        .withDaysSinceOnsetOfSymptoms(random.nextInt(13))
        .withSubmissionTimestamp(submissionTimestamp)
        .withVisitedCountries(Set.of("FR", "DK"))
        .withReportType(ReportType.CONFIRMED_TEST)
        .build();
  }

  private static byte[] randomByteData() {
    byte[] keyData = new byte[16];
    random.nextBytes(keyData);
    return keyData;
  }
}
