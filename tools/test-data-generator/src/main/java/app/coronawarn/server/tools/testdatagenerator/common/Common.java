package app.coronawarn.server.tools.testdatagenerator.common;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Common {

  public static int getRandomBetween(int minIncluding, int maxIncluding, Random random) {
    return Math.toIntExact(getRandomBetween(
        (long) minIncluding,
        (long) maxIncluding,
        random
    ));
  }

  public static long getRandomBetween(long minIncluding, long maxIncluding, Random random) {
    return minIncluding + (long) (random.nextDouble() * (maxIncluding - minIncluding));
  }

  public static int nextPoisson(int mean, Random random) {
    // https://stackoverflow.com/a/9832977
    // https://en.wikipedia.org/wiki/Poisson_distribution#Generating_Poisson-distributed_random_variables
    double L = Math.exp(-mean);
    int k = 0;
    double p = 1.0;
    do {
      p = p * random.nextDouble();
      k++;
    } while (p > L);
    return k - 1;
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

  public static <T> Consumer<T> uncheckedConsumer(
      CheckedConsumer<T, ? extends Exception> function) {
    return t -> {
      try {
        function.apply(t);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  /**
   * Partitions a list into {@code numPartitions} equally sized lists.
   *
   * @param list
   * @param numPartitions
   * @return A list of lists of equal size
   */
  public static <T> List<List<T>> partitionList(List<T> list, int numPartitions) {
    return IntStream.range(0, numPartitions)
        .mapToObj(currentPartition -> list.subList(
            numPartitions * currentPartition,
            Math.min(numPartitions * currentPartition + numPartitions, list.size()))
        )
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
   * Convert checked exceptions to unchecked exceptions in Consumers.
   */
  @FunctionalInterface
  public interface CheckedConsumer<T, E extends Exception> {

    void apply(T t) throws E;
  }

  @FunctionalInterface
  public interface Formatter<T> extends Function<T, String> {

    String apply(T t);
  }

}
