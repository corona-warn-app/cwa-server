package app.coronawarn.server.services.submission.checkins;

import static org.junit.jupiter.api.Assertions.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CheckinsDateSpecificationTest {

  @ParameterizedTest
  @MethodSource("unixTimestampsAndExpectations")
  void should_derive_10_minute_intervals_for_unix_timestamps_correctly(long timestamp,
      int expectation) {
    assertEquals(CheckinsDateSpecification.TEN_MINUTE_INTERVAL_DERIVATION.apply(timestamp),
        expectation);
  }

  @Test
  void should_not_derive_10_minute_intervals_for_invalid_unix_timestamps() {
    assertThrows(IllegalArgumentException.class,
        () -> CheckinsDateSpecification.TEN_MINUTE_INTERVAL_DERIVATION.apply(-1L));
  }

  private static Stream<Arguments> unixTimestampsAndExpectations() {
    return Stream.of(
        Arguments.of(1614675431, 2691125),
        Arguments.of(1614675614, 2691126));
  }
}
