package app.coronawarn.server.common.persistence.domain.validation.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RangeTest {

  @ParameterizedTest
  @ValueSource(ints = {-14, -13, 0, 10, 21})
  void testValueIsContained(Integer value) {
    Range<Integer> range = new Range<Integer>(-14, 21);
    assertTrue(range.contains(value));
  }

  @ParameterizedTest
  @ValueSource(ints = {-15, -17, 22})
  void testValueIsNotContained(Integer value) {
    Range<Integer> range = new Range<Integer>(-14, 21);
    assertFalse(range.contains(value));
  }

  @Test
  void testNullIsNotContained() {
    Range<Integer> range = new Range<Integer>(-14, 21);
    assertFalse(range.contains(null));
  }
}
