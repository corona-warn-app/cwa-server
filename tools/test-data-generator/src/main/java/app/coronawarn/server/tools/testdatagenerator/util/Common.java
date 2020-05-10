package app.coronawarn.server.tools.testdatagenerator.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.random.RandomGenerator;

public class Common {

  public static int getRandomBetween(int minIncluding, int maxIncluding, RandomGenerator random) {
    return Math.toIntExact(getRandomBetween(
        (long) minIncluding,
        maxIncluding,
        random
    ));
  }

  public static long getRandomBetween(long minIncluding, long maxIncluding,
      RandomGenerator random) {
    return minIncluding + (long) (random.nextDouble() * (maxIncluding - minIncluding));
  }

  public static <T, R> Function<T, R> uncheckedFunction(
      CheckedFunction<T, R, ? extends Exception> function) {
    return t -> {
      try {
        return function.apply(t);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  /**
   * Partitions a list into {@code numPartitions} equally sized lists.
   *
   * @param list          The list to partition
   * @param numPartitions The number of partitions
   * @return A list of lists of equal size
   */
  public static <T> List<List<T>> partitionList(List<T> list, int numPartitions) {
    int partitionSize = ceilDiv(list.size(), numPartitions);
    return IntStream.range(0, numPartitions)
        .mapToObj(currentPartition -> list.subList(
            partitionSize * currentPartition,
            Math.min(currentPartition * partitionSize + partitionSize, list.size()))
        )
        .collect(Collectors.toList());
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
    return -Math.floorDiv(-hours, 24);
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

  /**
   * Convert checked exceptions to unchecked exceptions in Functions.
   */
  @FunctionalInterface
  public interface CheckedFunction<T, R, E extends Exception> {

    R apply(T t) throws E;
  }

  /**
   * A {@link Function}.
   *
   * @param <T> The type of the elements to format.
   */
  @FunctionalInterface
  public interface Formatter<T> extends Function<T, Object> {

    Object apply(T t);
  }

  /**
   * A {@link Function}.
   *
   * @param <T> The type of the index elements.
   */
  @FunctionalInterface
  public interface IndexFunction<T> extends Function<Stack<Object>, List<T>> {

    List<T> apply(Stack<Object> t);
  }

  /**
   * A {@link Function}.
   */
  @FunctionalInterface
  public interface FileFunction extends Function<Stack<Object>, byte[]> {

    byte[] apply(Stack<Object> t);
  }

}
