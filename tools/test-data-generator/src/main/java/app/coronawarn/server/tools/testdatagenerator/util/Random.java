package app.coronawarn.server.tools.testdatagenerator.util;

import org.apache.commons.math3.random.RandomGenerator;

public class Random {

  public static int getRandomBetween(int minIncluding, int maxIncluding, RandomGenerator random) {
    return Math.toIntExact(getRandomBetween((long) minIncluding, maxIncluding, random));
  }

  public static long getRandomBetween(long minIncluding, long maxIncluding,
      RandomGenerator random) {
    return minIncluding + (long) (random.nextDouble() * (maxIncluding - minIncluding));
  }
}
