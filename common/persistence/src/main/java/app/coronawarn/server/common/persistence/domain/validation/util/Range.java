package app.coronawarn.server.common.persistence.domain.validation.util;

/**
 * Represents an inclusive range of values of the given type T that can be used for input validation
 * purposes.
 */
public class Range<T extends Comparable<T>> {

  private final T min;
  private final T max;

  public Range(T min, T max) {
    this.min = min;
    this.max = max;
  }

  /**
   * Returns true/false if the given value is part of the range.
   */
  public boolean contains(T value) {
    if (value == null) {
      return false;
    }
    return isGreaterOrEqual(value, min) && isLessOrEqual(value, max);
  }

  protected boolean isGreaterOrEqual(T t1, T t2) {
    return t1.compareTo(t2) >= 0;
  }

  protected boolean isLessOrEqual(T t1, T t2) {
    return t1.compareTo(t2) <= 0;
  }
}
