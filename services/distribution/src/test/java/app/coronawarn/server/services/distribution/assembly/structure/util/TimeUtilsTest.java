package app.coronawarn.server.services.distribution.assembly.structure.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class TimeUtilsTest {

  @Test
  void testGetCurrentUtcDateIsLocalDateNowInUtc() {
    assertEquals(LocalDate.now(ZoneOffset.UTC), TimeUtils.getUtcDate());
  }

  @Test
  void testGetUtcHour() {
    assertEquals(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS), TimeUtils.getCurrentUtcHour());
  }

  @Test
  void testGetNowIsLocalDateTimeInUtc() {
    final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    assertEquals(now, TimeUtils.getNow().truncatedTo(ChronoUnit.SECONDS));
    System.out.println(Instant.now());
  }

}
