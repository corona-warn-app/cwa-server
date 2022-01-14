

package app.coronawarn.server.common.shared.util;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.time.Duration;
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
  void testSetNowToNullRestoresOrigin() {
    Instant now = Instant.now();
    TimeUtils.setNow(now);

    assertEquals(TimeUtils.getNow(), now);

    TimeUtils.setNow(null);

    await().atLeast(Duration.ofMillis(10)).until(() -> {
      assertThat(now).isNotEqualTo(TimeUtils.getNow());
      return true;
    });

  }

  @Test
  void testNowIsUpdated() {
    Instant now = TimeUtils.getNow();

    await().atLeast(Duration.ofMillis(10)).until(() -> {
      assertThat(now).isNotEqualTo(Instant.now());
      return true;
    });
  }
}
