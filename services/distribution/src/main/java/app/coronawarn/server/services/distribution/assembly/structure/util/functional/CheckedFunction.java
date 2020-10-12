

package app.coronawarn.server.services.distribution.assembly.structure.util.functional;

import java.util.function.Function;

/**
 * Convert checked exceptions to unchecked exceptions in Functions.
 */
@FunctionalInterface
public interface CheckedFunction<T, R, E extends Exception> {

  R apply(T t) throws E;

  /**
   * Convert checked exceptions to unchecked exceptions in Functions.
   */
  static <T, R> Function<T, R> uncheckedFunction(
      CheckedFunction<T, R, ? extends Exception> function) {
    return input -> {
      try {
        return function.apply(input);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }
}
