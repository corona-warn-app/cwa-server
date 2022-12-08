package app.coronawarn.server.services.submission.config;

import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildMultipleKeys;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayload;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.config.TrlDerivations;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionServiceTrlMappingTest {

  public static DiagnosisKey randomKey() {
    final SubmissionType type = SubmissionType.SUBMISSION_TYPE_PCR_TEST;
    final byte[] id = new byte[16];
    new Random().nextBytes(id);

    return DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(id, type)
        .withRollingStartIntervalNumber(600)
        .withTransmissionRiskLevel(2)
        .withRollingPeriod(1)
        .withSubmissionTimestamp(0L)
        .withReportType(ReportType.CONFIRMED_TEST)
        .withCountryCode("DE")
        .build();
  }

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private RequestExecutor executor;

  @Autowired
  private SubmissionServiceConfig config;

  @MockBean
  private TanVerifier tanVerifier;

  @Test
  void checkKeyDataExistsWithHugeCollection() {
    final Collection<DiagnosisKey> keys = new LinkedList<>();
    for (int i = 0; i < config.getMaxNumberOfKeys(); i++) {
      keys.add(randomKey());
      keys.add(randomKey());
    }
    final int saved = diagnosisKeyService.saveDiagnosisKeys(keys);
    assertTrue(saved > config.getMaxNumberOfKeys(), "didn't save more keys, than allowed?");
    assertTrue(diagnosisKeyService.exists(keys));
  }

  @Test
  void checkResponseStatusForTrlMappingLoadingCorrectly() {
    final Map<Integer, Integer> expectedValues = Map.of(1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8);
    final ResponseEntity<Void> actResponse = executor.executePost(buildPayload(buildMultipleKeys(config)));
    assertThat(config.getTrlDerivations().getTrlMapping()).containsAllEntriesOf(expectedValues);
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void checkTrlMappingDerivation() {
    // buildMultipleKeys(config) creates keys with TRL 3, 6, 8.
    // The mappings for these values can be found in test/resources(3, 6, 8)
    final Collection<Integer> expectedTrlValues = List.of(3, 6, 8);

    final ResponseEntity<Void> actResponse = executor.executePost(buildPayload(buildMultipleKeys(config)));
    final Collection<DiagnosisKey> diagnosisKeyList = diagnosisKeyService.getDiagnosisKeys();
    diagnosisKeyList.forEach(diagnosisKey -> {
      assertThat(expectedTrlValues.contains(diagnosisKey.getTransmissionRiskLevel())).isTrue();
    });
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @BeforeEach
  void setupMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
  }

  @Test
  void testSubmissionServiceConfigValuesAreSet() {
    final TrlDerivations trlDerivations = new TrlDerivations();
    trlDerivations.setTrlMapping(Map.of(1, 1, 2, 2, 3, 3, 4, 4));
    config.setTrlDerivations(trlDerivations);

    assertThat(config.getTrlDerivations()).isNotNull();
    assertThat(config.getTrlDerivations().getTrlMapping()).hasSize(4);
  }
}
