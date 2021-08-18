package app.coronawarn.server.services.submission.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.verification.VerificationServerClient;
import feign.FeignException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SubmissionOnBehalfIT {

  @Autowired
  RequestExecutor executor;
  @MockBean
  VerificationServerClient verificationServerClient;

  @Test
  public void shouldReturn200WhenTanVerificationIsValid() {
    // given
    SubmissionPayload submissionPayload = validSubmissionBuilder()
        .build();
    HttpHeaders requestHeaders = buildDefaultRequestHeaders();
    HttpHeaders responseHeaders = buildTanTypeResponseHeader("EVENT");

    // when
    when(verificationServerClient.verifyTan(any()))
        .thenReturn(ResponseEntity.ok().headers(responseHeaders).build());
    ResponseEntity<Void> response = executor.executeSubmissionOnBehalf(submissionPayload, requestHeaders);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }


  @Test
  public void shouldReturn403WhenTanTypeIsNotEvent() {
    // given
    SubmissionPayload submissionPayload = validSubmissionBuilder()
        .build();

    // when
    when(verificationServerClient.verifyTan(any()))
        .thenReturn(ResponseEntity.ok().headers(buildTanTypeResponseHeader("NOT_EVENT")).build());
    ResponseEntity<Void> response = executor.executeSubmissionOnBehalf(submissionPayload);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }


  @Test
  public void shouldReturn403WhenTanVerificationFails() {
    // given
    SubmissionPayload submissionPayload = validSubmissionBuilder()
        .build();

    // when
    when(verificationServerClient.verifyTan(any()))
        .thenReturn(ResponseEntity.ok().headers(buildTanTypeResponseHeader("EVENT")).build());
    ResponseEntity<Void> response = executor.executeSubmissionOnBehalf(submissionPayload);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @ParameterizedTest
  @MethodSource("generateInvalidPayloads")
  public void shouldReturn400WhenUsingInvalidPayload() {
    // given
    SubmissionPayload submissionPayload = validSubmissionBuilder()
        .build();

    // when
    ResponseEntity<Void> response = executor.executeSubmissionOnBehalf(submissionPayload);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  static Stream<Arguments> generateInvalidPayloads() {
    return Stream.of(
        Arguments.of(validSubmissionBuilder().addAllKeys(DataHelpers.createValidTemporaryExposureKeys()).build()),
        Arguments.of(validSubmissionBuilder().addAllVisitedCountries(List.of("DE")).build()),
        Arguments.of(validSubmissionBuilder().setConsentToFederation(true).build()),
        Arguments.of(validSubmissionBuilder().setSubmissionType(SubmissionType.SUBMISSION_TYPE_PCR_TEST).build()),
        Arguments.of(validSubmissionBuilder().setSubmissionType(SubmissionType.SUBMISSION_TYPE_RAPID_TEST).build()),
        Arguments.of(validSubmissionBuilder().addAllCheckIns(List
            .of(DataHelpers.buildDefaultCheckIn(new byte[]{1, 2, 3}),
                DataHelpers.buildDefaultCheckIn(new byte[]{10, 20, 30}))).build()),
        Arguments.of(validSubmissionBuilder().addAllCheckInProtectedReports(Collections.emptyList()).build()),
        Arguments.of(validSubmissionBuilder().addAllCheckInProtectedReports(
            List.of(DataHelpers.buildDefaultEncryptedCheckIn(new byte[]{1, 2, 3}),
                DataHelpers.buildDefaultEncryptedCheckIn(new byte[]{10, 20, 30}))
        ).build())
    );
  }

  @Test
  void shouldReturn403IfTanIsNotUUID() {
    // given
    SubmissionPayload submissionPayload = validSubmissionBuilder()
        .build();
    HttpHeaders responseHeader = buildTanTypeResponseHeader("EVENT");
    HttpHeaders requestHeaders = buildDefaultRequestHeaders();
    requestHeaders.set("cwa-authorization", "not real uuid");

    // when
    when(verificationServerClient.verifyTan(any()))
        .thenReturn(ResponseEntity.ok().headers(responseHeader).build());
    ResponseEntity<Void> response = executor.executeSubmissionOnBehalf(submissionPayload, requestHeaders);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }


  @Test
  void shouldReturn403IfTanVerificationReturns404() {
    // given
    HttpHeaders requestHeaders = buildDefaultRequestHeaders();
    SubmissionPayload submissionPayload = validSubmissionBuilder()
        .build();

    // when
    when(verificationServerClient.verifyTan(any()))
        .thenThrow(FeignException.NotFound.class);
    ResponseEntity<Void> response = executor.executeSubmissionOnBehalf(submissionPayload, requestHeaders);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private static SubmissionPayload.Builder validSubmissionBuilder() {
    byte[] locationIdHash = new byte[32];
    new Random().nextBytes(locationIdHash);
    List<CheckInProtectedReport> protectedReports = List
        .of(DataHelpers.buildDefaultEncryptedCheckIn(locationIdHash),
            DataHelpers.buildDefaultEncryptedCheckIn(locationIdHash));
    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(), DataHelpers.buildDefaultCheckIn());
    return SubmissionPayload.newBuilder()
        .addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(Collections.emptyList())
        .setConsentToFederation(false)
        .setSubmissionType(SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns);
  }

  private HttpHeaders buildDefaultRequestHeaders() {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.set("cwa-authorization", UUID.randomUUID().toString());
    requestHeaders.set("cwa-fake", "0");
    return requestHeaders;
  }

  private HttpHeaders buildTanTypeResponseHeader(String event) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-CWA-TELETAN-TYPE", event);
    return headers;
  }
}
