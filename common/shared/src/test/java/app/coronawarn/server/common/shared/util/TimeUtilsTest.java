

package app.coronawarn.server.common.shared.util;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TimeUtilsTest {

  @Test
  void testGetCurrentUtcDateIsLocalDateNowInUtc() {
    assertEquals(LocalDate.now(UTC), TimeUtils.getUtcDate());
  }

  @Test
  void testGetUtcHour() {
    assertEquals(LocalDateTime.now(UTC).truncatedTo(HOURS), TimeUtils.getCurrentUtcHour());
  }

  @Test
  void testGetNowIsLocalDateTimeInUtc() {
    assertEquals(Instant.now().truncatedTo(HOURS), TimeUtils.getNow().truncatedTo(HOURS));
  }

  @Test
  void testSetNow() {
    Instant now = Instant.now();
    TimeUtils.setNow(now);

    assertEquals(TimeUtils.getNow(), now);
  }
}
