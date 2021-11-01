

package app.coronawarn.server.common.shared.functional;

import java.util.function.Consumer;

/**
 * Convert checked exceptions to unchecked exceptions in Consumers.
 */
@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception> {

  void apply(T t) throws E;

  /**
   * Convert checked exceptions to unchecked exceptions in Consumers.
   *
   * @param <T> T
   * @param consumer extends Exception
   * @return generic type
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
