

package app.coronawarn.server.common.shared.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

  private static Instant now;

  private TimeUtils() {
  }

  /**
   * Returns the UTC date and time at the beginning of the current hour.
   *
   * @return LocalDateTime of current UTC hour
   */
  public static LocalDateTime getCurrentUtcHour() {
    return LocalDateTime.ofInstant(getNow().truncatedTo(ChronoUnit.HOURS), ZoneOffset.UTC);
  }

  /**
   * Returns the UTC date.
   *
   * @return LocalDate of current UTC date
   */
  public static LocalDate getUtcDate() {
    return getCurrentUtcHour().toLocalDate();
  }

  /**
   * Returns the UTC {@link Instant} time or creates a new instance if called the first time.
   *
   * @return current Instant
   */
  public static Instant getNow() {
    if (now == null) {
      now = Instant.now();
    }
    return now;
  }

  /**
   * Injects UTC instant time value.
   *
   * @param instant an {@link Instant} object.
   */
  public static void setNow(Instant instant) {
    now = instant;
  }
}
