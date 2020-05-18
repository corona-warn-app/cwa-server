package app.coronawarn.server.common.persistence.service;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class DiagnosisKeyServiceTest {

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private DiagnosisKeyRepository diagnosisKeyRepository;

  @AfterEach
  public void tearDown() {
    diagnosisKeyRepository.deleteAll();
  }

  @Test
  void testRetrievalForEmptyDB() {
    var actKeys = diagnosisKeyService.getDiagnosisKeys();
    assertDiagnosisKeysEqual(Lists.emptyList(), actKeys);
  }

  @Test
  void testSaveAndRetrieve() {
    var expKeys = List.of(buildDiagnosisKeyForSubmissionTimestamp(0L));

    diagnosisKeyService.saveDiagnosisKeys(expKeys);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @Test
  void testSortedRetrievalResult() {
    var expKeys = new ArrayList<>(List.of(
        buildDiagnosisKeyForSubmissionTimestamp(1L),
        buildDiagnosisKeyForSubmissionTimestamp(0L)));

    diagnosisKeyService.saveDiagnosisKeys(expKeys);

    // reverse to match expected sort order
    Collections.reverse(expKeys);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @Test
  void testApplyRetentionPolicyForEmptyDb() {
    diagnosisKeyService.applyRetentionPolicy(1);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();
    assertDiagnosisKeysEqual(Lists.emptyList(), actKeys);
  }

  @Test
  void testApplyRetentionPolicyForOneNotApplicableEntry() {
    var expKeys = List.of(buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusHours(23)));

    diagnosisKeyService.saveDiagnosisKeys(expKeys);
    diagnosisKeyService.applyRetentionPolicy(1);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @Test
  void testApplyRetentionPolicyForOneApplicableEntry() {
    var keys = List.of(buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L)));

    diagnosisKeyService.saveDiagnosisKeys(keys);
    diagnosisKeyService.applyRetentionPolicy(1);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(Lists.emptyList(), actKeys);
  }

  private void assertDiagnosisKeysEqual(List<DiagnosisKey> expKeys, List<DiagnosisKey> actKeys) {
    assertEquals(expKeys.size(), actKeys.size(), "Cardinality mismatch");

    for (int i = 0; i < expKeys.size(); i++) {
      var expKey = expKeys.get(i);
      var actKey = actKeys.get(i);

      assertEquals(expKey.getKeyData(), actKey.getKeyData(), "keyData mismatch");
      assertEquals(expKey.getRollingStartNumber(), actKey.getRollingStartNumber(),
          "rollingStartNumber mismatch");
      assertEquals(expKey.getRollingPeriod(), actKey.getRollingPeriod(),
          "rollingPeriod mismatch");
      assertEquals(expKey.getTransmissionRiskLevel(), actKey.getTransmissionRiskLevel(),
          "transmissionRiskLevel mismatch");
      assertEquals(expKey.getSubmissionTimestamp(), actKey.getSubmissionTimestamp(),
          "submissionTimestamp mismatch");
    }
  }

  public static DiagnosisKey buildDiagnosisKeyForSubmissionTimestamp(long submissionTimeStamp) {
    return DiagnosisKey.builder()
        .withKeyData(new byte[16])
        .withRollingStartNumber(600L)
        .withRollingPeriod(1L)
        .withTransmissionRiskLevel(2)
        .withSubmissionTimestamp(submissionTimeStamp).build();
  }

  public static DiagnosisKey buildDiagnosisKeyForDateTime(OffsetDateTime dateTime) {
    return buildDiagnosisKeyForSubmissionTimestamp(dateTime.toEpochSecond() / 3600);
  }
}
