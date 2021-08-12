package app.coronawarn.server.services.submission.integration;

import static app.coronawarn.server.services.submission.integration.DataHelpers.buildDefaultCheckIn;
import static app.coronawarn.server.services.submission.integration.DataHelpers.buildDefaultEncryptedCheckIn;
import static app.coronawarn.server.services.submission.integration.DataHelpers.buildSubmissionPayloadWithCheckins;
import static app.coronawarn.server.services.submission.integration.DataHelpers.createValidTemporaryExposureKeys;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.controller.FakeDelayManager;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles({"integration-test", "disable-unencrypted-checkins"})
class SubmissionDisabledUnencryptedCheckInsIT {


  @Autowired
  private RequestExecutor executor;

  @Autowired
  private SubmissionServiceConfig config;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private TestRestTemplate testRestTemplate;


  @MockBean
  private TanVerifier tanVerifier;

  @MockBean
  private FakeDelayManager fakeDelayManager;

  @BeforeEach
  public void setUpMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
    when(fakeDelayManager.getJitteredFakeDelay()).thenReturn(1000L);
  }


  @Test
  void unencryptedCheckInsDisabledShouldResultInSavingLessNumberOfCheckIns() {
    // GIVEN:
    List<String> visitedCountries = Collections.singletonList("DE");
    String originCountry = "DE";

    List<TemporaryExposureKey> temporaryExposureKeys = createValidTemporaryExposureKeys();
    List<CheckInProtectedReport> protectedReports = Collections.singletonList(buildDefaultEncryptedCheckIn());
    List<CheckIn> checkins = Collections.singletonList(
        buildDefaultCheckIn());
    SubmissionPayload submissionPayload = buildSubmissionPayloadWithCheckins(visitedCountries, originCountry,
        true,
        temporaryExposureKeys, SubmissionType.SUBMISSION_TYPE_PCR_TEST, protectedReports, checkins);

    // WHEN:
    ResponseEntity<Void> result = executor.executePost(submissionPayload);

    // THEN:
    // DISABLED: For the one valid unencrypted checkin we generate one fake checkins
    // But only allow the unencrypted checkin, which is 1 = 1 Saved checkins
    assertThat(result.getHeaders().get("cwa-filtered-checkins").get(0)).isEqualTo("0");
    assertThat(result.getHeaders().get("cwa-saved-checkins").get(0)).isEqualTo("1");
  }
}
