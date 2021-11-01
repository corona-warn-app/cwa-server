

package app.coronawarn.server.common.shared.util;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

  @Test
  void testSetNowToNullRestoresOrigin() throws InterruptedException {
    Instant now = Instant.now();
    TimeUtils.setNow(now);

    assertEquals(TimeUtils.getNow(), now);

    TimeUtils.setNow(null);
    Thread.sleep(10);
    assertNotEquals(now, TimeUtils.getNow());
  }

  @Test
  void testNowIsUpdated() throws InterruptedException {
    Instant now = TimeUtils.getNow();
    Thread.sleep(10);
    assertNotEquals(now, Instant.now());
  }
}
