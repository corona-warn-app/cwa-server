package app.coronawarn.server.common.persistence.utils;

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

  @ParameterizedTest
  @MethodSource("hourUnixTimestampsAndExpectations")
  void should_derive_1_hour_minute_intervals_for_unix_timestamps_correctly(long timestamp,
      int expectation) {
    assertEquals(CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION.apply(timestamp),
        expectation);
  }

  private static Stream<Arguments> unixTimestampsAndExpectations() {
    return Stream.of(
        Arguments.of(1614675431, 2691125),
        Arguments.of(1614675614, 2691126));
  }

  private static Stream<Arguments> hourUnixTimestampsAndExpectations() {
    return Stream.of(
        Arguments.of(1614675431, 448520),
        Arguments.of(1614675614, 448521));
  }
}
