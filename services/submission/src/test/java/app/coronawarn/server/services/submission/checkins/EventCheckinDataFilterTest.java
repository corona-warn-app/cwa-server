package app.coronawarn.server.services.submission.checkins;

import static app.coronawarn.server.services.submission.checkins.CheckinsDateSpecification.TEN_MINUTE_INTERVAL_DERIVATION;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.google.protobuf.ByteString;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.protocols.internal.pt.SignedTraceLocation;
import app.coronawarn.server.common.protocols.internal.pt.TraceLocation;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig.Payload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig.Payload.Checkins;

class EventCheckinDataFilterTest {

  private static final int ACCEPTABLE_EVENT_DATE_THRESHOLD_IN_DAYS = 15;

  private EventCheckinDataFilter underTest;
  private SubmissionServiceConfig mockConfig;

  @BeforeEach
  public void setup() {
    mockConfig = new SubmissionServiceConfig();
    Payload payloadConfig = new Payload();
    payloadConfig.setCheckins(new Checkins());
    mockConfig.setPayload(payloadConfig);
    mockConfig.setAcceptedEventDateThresholdDays(ACCEPTABLE_EVENT_DATE_THRESHOLD_IN_DAYS);

    TraceLocationSignatureVerifier locationSignatureVerifier =
        mock(TraceLocationSignatureVerifier.class);
    when(locationSignatureVerifier.verify(any())).thenReturn(true);
    underTest = new EventCheckinDataFilter(mockConfig, locationSignatureVerifier);
  }

  @ParameterizedTest
  @ValueSource(ints = {ACCEPTABLE_EVENT_DATE_THRESHOLD_IN_DAYS, 16, 19, 20})
  void should_filter_out_checkins_for_old_events(int daysInThePast) {
    Instant thisTimeInstant = Instant.now();
    long eventCheckoutInThePast =
        LocalDateTime.ofInstant(thisTimeInstant, UTC).minusDays(daysInThePast).toEpochSecond(UTC);

    long acceptableEventCheckoutDate =
        LocalDateTime.ofInstant(thisTimeInstant, UTC).minusDays(1).toEpochSecond(UTC);

    SubmissionPayload newPayload =
        SubmissionPayload.newBuilder()
            .addAllCheckIns(List.of(
                CheckIn.newBuilder()
                    .setStartIntervalNumber(
                        TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckoutInThePast - 10))
                    .setEndIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckoutInThePast))
                    .setTransmissionRiskLevel(1).build(),
                CheckIn.newBuilder()
                    .setStartIntervalNumber(
                        TEN_MINUTE_INTERVAL_DERIVATION.apply(acceptableEventCheckoutDate - 10))
                    .setEndIntervalNumber(
                        TEN_MINUTE_INTERVAL_DERIVATION.apply(acceptableEventCheckoutDate))
                    .setTransmissionRiskLevel(3).build()))
            .build();

    List<CheckIn> result = underTest.extractAndFilter(newPayload);
    assertEquals(result.size(), 1);
    CheckIn filteredCheckin = result.iterator().next();
    assertEquals(filteredCheckin.getStartIntervalNumber(),
        TEN_MINUTE_INTERVAL_DERIVATION.apply(acceptableEventCheckoutDate - 10));
    assertEquals(filteredCheckin.getEndIntervalNumber(),
        TEN_MINUTE_INTERVAL_DERIVATION.apply(acceptableEventCheckoutDate));
    assertEquals(filteredCheckin.getTransmissionRiskLevel(), 3);
  }

  @ParameterizedTest
  @ValueSource(ints = {ACCEPTABLE_EVENT_DATE_THRESHOLD_IN_DAYS - 1,
      ACCEPTABLE_EVENT_DATE_THRESHOLD_IN_DAYS - 2})
  void should_not_filter_checkins_of_events_with_acceptable_date_thresholds(int daysInThePast) {
    Instant thisTimeInstant = Instant.now();
    long eventCheckoutInThePast =
        LocalDateTime.ofInstant(thisTimeInstant, UTC).minusDays(daysInThePast).toEpochSecond(UTC);

    long acceptableEventCheckoutDate =
        LocalDateTime.ofInstant(thisTimeInstant, UTC).minusDays(1).toEpochSecond(UTC);

    SubmissionPayload newPayload =
        SubmissionPayload.newBuilder()
            .addAllCheckIns(List.of(
                CheckIn.newBuilder()
                    .setStartIntervalNumber(
                        TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckoutInThePast - 10))
                    .setEndIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckoutInThePast))
                    .setTransmissionRiskLevel(1).build(),
                CheckIn.newBuilder()
                    .setStartIntervalNumber(
                        TEN_MINUTE_INTERVAL_DERIVATION.apply(acceptableEventCheckoutDate - 10))
                    .setEndIntervalNumber(
                        TEN_MINUTE_INTERVAL_DERIVATION.apply(acceptableEventCheckoutDate))
                    .setTransmissionRiskLevel(3).build()))
            .build();

    List<CheckIn> result = underTest.extractAndFilter(newPayload);
    assertEquals(result.size(), 2);
  }

  @Test
  void should_filter_out_checkins_for_future_events() {
    Instant thisTimeInstant = Instant.now();
    long eventCheckinInTheFuture =
        LocalDateTime.ofInstant(thisTimeInstant, UTC).plusMinutes(11).toEpochSecond(UTC);

    long eventCheckinInTheNearPast =
        LocalDateTime.ofInstant(thisTimeInstant, UTC).minusMinutes(10).toEpochSecond(UTC);

    SubmissionPayload newPayload =
        SubmissionPayload.newBuilder()
            .addAllCheckIns(List.of(
                CheckIn.newBuilder()
                    .setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheFuture))
                    .setEndIntervalNumber(
                        TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheFuture + 5))
                    .setTransmissionRiskLevel(1).build(),
                CheckIn.newBuilder()
                    .setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheNearPast))
                    .setEndIntervalNumber(
                        TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheNearPast + 10))
                    .setTransmissionRiskLevel(3).build()))
            .build();

    List<CheckIn> result = underTest.extractAndFilter(newPayload);
    assertEquals(result.size(), 1);
    CheckIn filteredCheckin = result.iterator().next();
    assertEquals(filteredCheckin.getStartIntervalNumber(),
        TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheNearPast));
    assertEquals(filteredCheckin.getEndIntervalNumber(),
        TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheNearPast + 10));
    assertEquals(filteredCheckin.getTransmissionRiskLevel(), 3);
  }

  @Test
  void should_filter_out_checkins_which_do_not_pass_signature_verification() {

    TraceLocationSignatureVerifier mockSignatureVerifier =
        mock(TraceLocationSignatureVerifier.class);
    EventCheckinDataFilter filter = new EventCheckinDataFilter(mockConfig, mockSignatureVerifier);


    SignedTraceLocation validEvent = SignedTraceLocation.newBuilder().setLocation(TraceLocation.newBuilder().build().toByteString())
        .setSignature(ByteString.copyFrom("valid".getBytes())).build();
    SignedTraceLocation invalidEvent = SignedTraceLocation.newBuilder().setLocation(TraceLocation.newBuilder().build().toByteString())
        .setSignature(ByteString.copyFrom("invalid".getBytes())).build();


    when(mockSignatureVerifier.verify(eq(validEvent))).thenReturn(true);
    when(mockSignatureVerifier.verify(eq(invalidEvent))).thenReturn(false);

    long eventCheckinInTheNearPast =
        LocalDateTime.ofInstant(Instant.now(), UTC).minusMinutes(10).toEpochSecond(UTC);

    SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(
            CheckIn.newBuilder()
                .setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheNearPast))
                .setEndIntervalNumber(
                    TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheNearPast + 2))
                .setSignedLocation(validEvent).setTransmissionRiskLevel(1).build(),
            CheckIn.newBuilder()
                .setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheNearPast))
                .setEndIntervalNumber(
                    TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheNearPast + 3))
                .setSignedLocation(invalidEvent).setTransmissionRiskLevel(3).build()))
        .build();

    List<CheckIn> result = filter.extractAndFilter(newPayload);
    assertEquals(result.size(), 1);
    CheckIn filteredCheckin = result.iterator().next();
    assertEquals(filteredCheckin.getStartIntervalNumber(),
        TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheNearPast));
    assertEquals(filteredCheckin.getEndIntervalNumber(),
        TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheNearPast + 2));
    assertEquals(filteredCheckin.getTransmissionRiskLevel(), 1);
  }
}
