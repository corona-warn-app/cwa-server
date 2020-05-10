package app.coronawarn.server.tools.testdatagenerator.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.random.RandomGenerator;

public class Maths {

  public static int getRandomBetween(int minIncluding, int maxIncluding, RandomGenerator random) {
    return Math.toIntExact(getRandomBetween((long) minIncluding, maxIncluding, random));
  }

  public static long getRandomBetween(long minIncluding, long maxIncluding,
      RandomGenerator random) {
    return minIncluding + (long) (random.nextDouble() * (maxIncluding - minIncluding));
  }

  public static int ceilDiv(int numerator, int denominator) {
    return (numerator + denominator - 1) / denominator;
  }

  /**
   * Calculates the number of days covered by {@code hours} (rounded up) Examples: {@code
   * (assert(getNumberOfDays(23) == 1); assert(getNumberOfDays(24) == 1); assert(getNumberOfDays(25)
   * == 2);}.
   */
  public static int getNumberOfDays(int hours) {
    return ceilDiv(hours, 24);
  }

  /**
   * Creates a list of all {@link LocalDate LocalDates} between {@code startDate} and {@code
   * numDays} later.
   */
  public static List<LocalDate> getDates(LocalDate startDate, int numDays) {
    return IntStream.range(0, numDays)
        .mapToObj(startDate::plusDays)
        .collect(Collectors.toList());
  }

  /**
   * Creates a list of all {@link LocalDateTime LocalDateTimes} between {@code startDate} and {@code
   * currentDate} (at 00:00 UTC) plus {@code totalHours % 24}.
   */
  public static List<LocalDateTime> getHours(LocalDate startDate, LocalDate currentDate,
      int totalHours) {
    int numFullDays = Math.floorDiv(totalHours, 24);
    long currentDay = ChronoUnit.DAYS.between(startDate, currentDate);
    int lastHour = (currentDay < numFullDays) ? 24 : totalHours % 24;
    return IntStream.range(0, lastHour)
        .mapToObj(hour -> currentDate.atStartOfDay().plusHours(hour))
        .collect(Collectors.toList());
  }
}
