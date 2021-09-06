

package app.coronawarn.server.common.shared.util;

import static java.time.ZoneOffset.UTC;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUtils {

  private static final Logger logger = LoggerFactory
      .getLogger(TimeUtils.class);

  private static Clock clock = Clock.systemUTC();

  private TimeUtils() {
  }

  /**
   * Returns the UTC date and time at the beginning of the current hour.
   *
   * @return LocalDateTime of current UTC hour
   */
  public static LocalDateTime getCurrentUtcHour() {
    return LocalDateTime.ofInstant(getNow().truncatedTo(ChronoUnit.HOURS), UTC);
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
    return Instant.now(clock);
  }

  /**
   * Derive local date at UTC zone to epoch seconds.
   *
   * @return - to epoch seconds
   */
  public static long toEpochSecondsUtc(LocalDate localDate) {
    return localDate.atStartOfDay(UTC).toEpochSecond();
  }

  /**
   * Injects UTC instant time value.<br />
   *
   * <strong>NOTE: THIS IS ONLY FOR TESTING PURPOSES!</strong>
   *
   * @param instant an {@link Instant} as a fixed time to set.
   */
  public static void setNow(Instant instant) {
    if (instant == null) {
      clock = Clock.systemUTC();
      return;
    }
    logger.warn("Setting the clock to a fixed time. THIS SHOULD NEVER BE USED IN PRODUCTION!");
    clock = Clock.fixed(instant, UTC);
  }
}
