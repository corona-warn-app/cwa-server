package app.coronawarn.server.services.submission.integration;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.verification.VerificationServerClient;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static app.coronawarn.server.services.submission.integration.DataHelpers.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SubmissionOnBehalfIT {

  @Autowired
  RequestExecutor executor;
  @MockBean
  VerificationServerClient verificationServerClient;

  @ParameterizedTest
  @ValueSource(strings = {"EVENT", "Event", "event"})
  @DisplayName("Should return 200 OK if payload is valid and tan verification is successful and response header == EVENT")
  public void shouldReturn200WhenTanVerificationIsValid(String teleTanType) {
    // given
    SubmissionPayload submissionPayload = validSubmissionBuilder()
        .build();
    HttpHeaders requestHeaders = buildDefaultRequestHeaders();
    HttpHeaders responseHeaders = buildTeleTanTypeResponseHeader(teleTanType);

    // when
    when(verificationServerClient.verifyTan(any()))
        .thenReturn(ResponseEntity.ok().headers(responseHeaders).build());
    ResponseEntity<Void> response = executor.executeSubmissionOnBehalf(submissionPayload, requestHeaders);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  @DisplayName("Should return 200 OK if payload is valid but contains empty checkins and tan verification is successful and response header == EVENT")
  public void shouldReturn200WhenEvenWhenCheckInsAreEmpty() {
    // given
    SubmissionPayload submissionPayload = validSubmissionBuilder()
        .addAllCheckIns(Collections.emptyList())
        .build();
    HttpHeaders requestHeaders = buildDefaultRequestHeaders();
    HttpHeaders responseHeaders = buildTeleTanTypeResponseHeader("EVENT");

    // when
    when(verificationServerClient.verifyTan(any()))
        .thenReturn(ResponseEntity.ok().headers(responseHeaders).build());
    ResponseEntity<Void> response = executor.executeSubmissionOnBehalf(submissionPayload, requestHeaders);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }


  @Test
  @DisplayName("Should return 403 when payload is valid and tan verification is successful BUT response header is NOT EVENT")
  public void shouldReturn403WhenTanTypeIsNotEvent() {
    // given
    SubmissionPayload submissionPayload = validSubmissionBuilder()
        .build();

    // when
    when(verificationServerClient.verifyTan(any()))
        .thenReturn(ResponseEntity.ok().headers(buildTeleTanTypeResponseHeader("NOT_EVENT")).build());
    ResponseEntity<Void> response = executor.executeSubmissionOnBehalf(submissionPayload);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }


  @Test
  @DisplayName("Should return 403 when payload is valid and tan verification fails")
  public void shouldReturn403WhenTanVerificationFails() {
    // given
    SubmissionPayload submissionPayload = validSubmissionBuilder()
        .build();

    // when
    when(verificationServerClient.verifyTan(any()))
        .thenReturn(ResponseEntity.ok().headers(buildTeleTanTypeResponseHeader("EVENT")).build());
    ResponseEntity<Void> response = executor.executeSubmissionOnBehalf(submissionPayload);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @ParameterizedTest
  @DisplayName("Should return 403 when payload is INVALID")
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
        Arguments.of(validSubmissionBuilder().addAllKeys(createValidTemporaryExposureKeys()).build()),
        Arguments.of(validSubmissionBuilder().addAllVisitedCountries(List.of("DE")).build()),
        Arguments.of(validSubmissionBuilder().setConsentToFederation(true).build()),
        Arguments.of(validSubmissionBuilder().setSubmissionType(SubmissionType.SUBMISSION_TYPE_PCR_TEST).build()),
        Arguments.of(validSubmissionBuilder().setSubmissionType(SubmissionType.SUBMISSION_TYPE_RAPID_TEST).build()),
        Arguments.of(validSubmissionBuilder().addAllCheckIns(List
            .of(buildDefaultCheckIn(new byte[]{1, 2, 3}),
                buildDefaultCheckIn(new byte[]{10, 20, 30}))).build()),
        Arguments.of(validSubmissionBuilder().addAllCheckInProtectedReports(Collections.emptyList()).build()),
        Arguments.of(validSubmissionBuilder().addAllCheckInProtectedReports(
            List.of(buildDefaultEncryptedCheckIn(buildValidLocationIdHash()),
                buildDefaultEncryptedCheckIn(buildDifferentCorrectLocationIdHash()))
        ).build()),
        Arguments.of(validSubmissionBuilder()
            .addAllCheckInProtectedReports(List.of(buildDefaultEncryptedCheckIn(new byte[]{1, 2, 3}))))
    );
  }

  static byte[] buildValidLocationIdHash() {
    return new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
        28, 29, 30, 31, 32};
  }

  static byte[] buildDifferentCorrectLocationIdHash() {
    byte[] bytes = buildValidLocationIdHash();
    bytes[0] = 10;
    return bytes;
  }

  @Test
  @DisplayName("Should return 403 when payload is valid but cwa-authorization header is not a valid UUID")
  void shouldReturn403IfTanIsNotUUID() {
    // given
    SubmissionPayload submissionPayload = validSubmissionBuilder()
        .build();
    HttpHeaders responseHeader = buildTeleTanTypeResponseHeader("EVENT");
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
  @DisplayName("Should return 403 when payload is valid and tan verification returns a 404")
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
        .of(buildDefaultEncryptedCheckIn(locationIdHash),
            buildDefaultEncryptedCheckIn(locationIdHash));
    List<CheckIn> checkIns = List
        .of(buildDefaultCheckIn(), buildDefaultCheckIn());
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

  private HttpHeaders buildTeleTanTypeResponseHeader(String event) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-CWA-TELETAN-TYPE", event);
    return headers;
  }
}
