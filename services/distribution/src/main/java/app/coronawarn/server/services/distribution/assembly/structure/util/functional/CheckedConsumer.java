

package app.coronawarn.server.services.distribution.assembly.structure.util.functional;

import java.util.function.Consumer;

/**
 * Convert checked exceptions to unchecked exceptions in Consumers.
 */
@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception> {

  void apply(T t) throws E;

  /**
   * Convert checked exceptions to unchecked exceptions in Consumers.
   */
  static <T> Consumer<T> uncheckedConsumer(CheckedConsumer<T, ? extends Exception> consumer) {
    return input -> {
      try {
        consumer.apply(input);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }
}
