package app.coronawarn.server.services.submission.checkins;

import static app.coronawarn.server.services.submission.checkins.CheckinsDateSpecification.TEN_MINUTE_INTERVAL_DERIVATION;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.evreg.CheckIn;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig.Payload;

class EventCheckinDataFilterTest {

  private static final int ACCEPTABLE_EVENT_DATE_THRESHOLD_IN_DAYS = 15;
  private static final int CORRECT_TRL = 1;
  private static final int CORRECT_CHECKOUT_TIME = 12;
  private static final int CORRECT_CHECKIN_TIME = 1;

  private EventCheckinDataFilter underTest;

  @BeforeEach
  public void setup() {
    SubmissionServiceConfig mockConfig = new SubmissionServiceConfig();
    mockConfig.setPayload(new Payload());
    mockConfig.setCheckinRetentionDate(ACCEPTABLE_EVENT_DATE_THRESHOLD_IN_DAYS);
    underTest = new EventCheckinDataFilter(mockConfig);
  }

  @ParameterizedTest
  @ValueSource(ints = {ACCEPTABLE_EVENT_DATE_THRESHOLD_IN_DAYS, 16, 19, 20})
  void should_filter_out_checkins_for_old_events(int daysInThePast) {
    Instant thisTimeInstant = Instant.now();
    long eventCheckoutInThePast =
        LocalDateTime.ofInstant(thisTimeInstant, UTC).minusDays(daysInThePast).toEpochSecond(UTC);

    long acceptableEventCheckoutDate =
        LocalDateTime.ofInstant(thisTimeInstant, UTC).minusDays(1).toEpochSecond(UTC);

    SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(
            CheckIn.newBuilder()
             .setCheckinTime(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckoutInThePast - 10))
             .setCheckoutTime(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckoutInThePast))
             .setTrl(CORRECT_TRL).build(),
            CheckIn.newBuilder()
             .setCheckinTime(TEN_MINUTE_INTERVAL_DERIVATION.apply(acceptableEventCheckoutDate - 10))
             .setCheckoutTime(TEN_MINUTE_INTERVAL_DERIVATION.apply(acceptableEventCheckoutDate))
             .setTrl(3).build()))
        .build();

    List<CheckIn> result = underTest.extractAndFilter(newPayload);
    assertEquals(result.size(), 1);
    CheckIn filteredCheckin = result.iterator().next();
    assertEquals(filteredCheckin.getCheckinTime(), TEN_MINUTE_INTERVAL_DERIVATION.apply(acceptableEventCheckoutDate - 10));
    assertEquals(filteredCheckin.getCheckoutTime(), TEN_MINUTE_INTERVAL_DERIVATION.apply(acceptableEventCheckoutDate));
    assertEquals(filteredCheckin.getTrl(), 3);
  }

  @ParameterizedTest
  @ValueSource(ints = {ACCEPTABLE_EVENT_DATE_THRESHOLD_IN_DAYS - 1, ACCEPTABLE_EVENT_DATE_THRESHOLD_IN_DAYS -2})
  void should_not_filter_checkins_of_events_with_acceptable_date_thresholds(int daysInThePast) {
    Instant thisTimeInstant = Instant.now();
    long eventCheckoutInThePast =
        LocalDateTime.ofInstant(thisTimeInstant, UTC).minusDays(daysInThePast).toEpochSecond(UTC);

    long acceptableEventCheckoutDate =
        LocalDateTime.ofInstant(thisTimeInstant, UTC).minusDays(1).toEpochSecond(UTC);

    SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(
            CheckIn.newBuilder()
             .setCheckinTime(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckoutInThePast - 10))
             .setCheckoutTime(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckoutInThePast))
             .setTrl(CORRECT_TRL).build(),
            CheckIn.newBuilder()
             .setCheckinTime(TEN_MINUTE_INTERVAL_DERIVATION.apply(acceptableEventCheckoutDate - 10))
             .setCheckoutTime(TEN_MINUTE_INTERVAL_DERIVATION.apply(acceptableEventCheckoutDate))
             .setTrl(3).build()))
        .build();

    List<CheckIn> result = underTest.extractAndFilter(newPayload);
    assertEquals(result.size(), 2);
  }
}
