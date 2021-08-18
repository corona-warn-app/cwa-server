package app.coronawarn.server.services.submission.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.verification.VerificationServerClient;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SubmissionOnBehalfIT {

  @Autowired
  RequestExecutor executor;
  @MockBean
  VerificationServerClient verificationServerClient;

  /**
   * INTEGRATION TEST 1 (shouldReturn200WhenTanVerificationIsValid) Execute POST using a payload
   * <p>
   * // given: A valid payload, a valid tan
   * <p>
   * // when: Black box 1. The request should return 200 2. Header ‘X-CWA-TELETAN-TYPE’ == EVENT
   * <p>
   * // then: OK
   */


  @Test
  public void shouldReturn200WhenTanVerificationIsValid() {

    // given a valid payload
    byte[] locationIdHash = new byte[32];
    new Random().nextBytes(locationIdHash);
    List<CheckInProtectedReport> protectedReports = List
        .of(DataHelpers.buildDefaultEncryptedCheckIn(locationIdHash),
            DataHelpers.buildDefaultEncryptedCheckIn(locationIdHash));
    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(), DataHelpers.buildDefaultCheckIn());

    SubmissionPayload submissionPayload = SubmissionPayload.newBuilder()
        .addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(Collections.emptyList())
        .setConsentToFederation(false)
        .setSubmissionType(SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns)
        .build();

    String tan = "fsjkfl";

    when(verificationServerClient.verifyTan(any()))
        .thenReturn(ResponseEntity.ok().header("X-CWA-TELETAN-TYPE", "EVENT").build());

    ResponseEntity<Void> response = executor.executeSubmissionOnBehalf(submissionPayload);


  }
}
