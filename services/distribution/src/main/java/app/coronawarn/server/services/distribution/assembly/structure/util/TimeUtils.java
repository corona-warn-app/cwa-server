package app.coronawarn.server.services.distribution.assembly.structure.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TimeUtils {

  private static LocalDate utcDate;
  private static LocalDateTime utcDateTime;
  private static LocalDateTime utcHour;
  private static Instant now;

  public static LocalDateTime getCurrentUtcHour() {
    if (utcHour == null) {
      utcHour = LocalDateTime.now(ZoneOffset.UTC);
    }
    return utcHour;
  }

  public static LocalDate getCurrentUtcDay() {
    if (utcDate == null) {
      utcDate = LocalDate.now(ZoneOffset.UTC);
    }
    return utcDate;
  }

  public static LocalDateTime getCurrentUtcDateTime() {
    if (utcDateTime == null) {
      utcDateTime = LocalDateTime.now(ZoneOffset.UTC);
    }
    return utcDateTime;
  }

  public static Instant getNow() {
    if (now == null) {
      now = Instant.now();
    }
    return now;
  }

  public static void setNow(Instant now) {
    TimeUtils.now = now;
  }

  public static void setUtcDate(LocalDate utcDate) {
    TimeUtils.utcDate = utcDate;
  }

  public static void setUtcDateTime(LocalDateTime utcDateTime) {
    TimeUtils.utcDateTime = utcDateTime;
  }

  public static void setUtcHour(LocalDateTime utcHour) {
    TimeUtils.utcHour = utcHour;
  }
}
