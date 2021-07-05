package jupiterHelpers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import app.coronawarn.server.junit.DisabledAroundMidnight;
import org.junit.jupiter.api.Test;

class DisabledAroundMidnightConditionTest {

  @Test
  @DisabledAroundMidnight(offsetInMinutes = 12 * 60)
  void testDisabledAroundMidnight() {
    fail("Test should be disabled and therefore NOT fail!");
  }

  @Test
  @DisabledAroundMidnight(offsetInMinutes = 0)
  void testEndabledAroundMidnight() {
    assertTrue(true, "Test should always be executed!");
  }
}
