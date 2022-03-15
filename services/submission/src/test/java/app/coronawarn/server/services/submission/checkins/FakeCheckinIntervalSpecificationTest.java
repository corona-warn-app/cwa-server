package app.coronawarn.server.services.submission.checkins;

import static app.coronawarn.server.services.submission.checkins.FakeCheckinIntervalSpecification.START_INTERVAL_GENERATION;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class FakeCheckinIntervalSpecificationTest {

  static IntStream rangeProvider() {
    return IntStream.range(0, 1000);
  }

  boolean between(final int min, final int value, final int max) {
    return min <= value && value <= max;
  }

  /**
   * Check with for the range provided by {@link #rangeProvider()}
   * @param startIntervalNumber
   */
  @ParameterizedTest(name = "#{index} - Test with Int : {0}")
  @MethodSource("rangeProvider")
  void testStartIntervalGeneration(final int startIntervalNumber) {
    final CheckIn checkIn = CheckIn.newBuilder().setStartIntervalNumber(startIntervalNumber).build();
    assertTrue(between(startIntervalNumber - 5, START_INTERVAL_GENERATION.apply(checkIn), startIntervalNumber + 5));
  }

  /**
   * When divided by 144, we end up at the same value.
   */
  @Test
  void testStartIntervalGeneration0() {
    CheckIn checkIn = CheckIn.newBuilder().setStartIntervalNumber(0).build();
    assertEquals(0, START_INTERVAL_GENERATION.apply(checkIn));
    checkIn = CheckIn.newBuilder().setStartIntervalNumber(144).build();
    assertEquals(144, START_INTERVAL_GENERATION.apply(checkIn));
  }

  /**
   * After 1000 calls with the same StartIntervalNumber, we should have 11 different values (1x the original, plus the
   * ranges origin - 5 till origin + 5).
   */
  @Test
  void testStartIntervalGenerationSet() {
    final Set<Integer> set = new HashSet<>();
    assertEquals(0, set.size());
    for (int i = 0; i < 1000; i++) {
      final CheckIn checkIn = CheckIn.newBuilder().setStartIntervalNumber(2698815).build();
      set.add(START_INTERVAL_GENERATION.apply(checkIn));
    }
    assertEquals(11, set.size());
  }
}
